package io.metersphere.service;

import com.google.common.util.concurrent.AtomicDouble;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.db.mapper.DataRulesMapper;
import io.metersphere.db.mapper.DataRulesNodeMapper;
import io.metersphere.db.mapper.ext.ExtDataRulesNodeMapper;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.request.DragDataRulesRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DataRulesNodeService extends NodeTreeService<DataRulesNodeDTO>{
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtDataRulesNodeMapper extDataRulesNodeMapper;

    @Resource
    private DataRulesNodeMapper dataRulesNodeMapper;
    @Resource
    private DataRulesMapper dataRulesMapper;
    
    private static final String DATA_RULES_NODE_CREATE_KEY = "DATA_RULES:DEFAULT_NODE:CREATE";

    public DataRulesNodeService(DataRulesNodeMapper dataRulesNodeMapper) {
        super(DataRulesNodeDTO.class);
        this.dataRulesNodeMapper = dataRulesNodeMapper;
    }

    public List<DataRulesNodeDTO> getNodeTreeByProjectId(String projectId, QueryDataRulesRequest request) {
        checkDefaultNode(projectId);
//        request.setNodeIds(null);
        List<DataRulesNodeDTO> countNodes = extDataRulesNodeMapper.getCountNodes(request);
        List<DataRulesNodeDTO> testCaseNodes = extDataRulesNodeMapper.getNodeTreeByProjectId(projectId);
        return getNodeTrees(testCaseNodes, getCountMap(countNodes));
    }

    public String addNode(DataRulesNode node) {
        validateNode(node);
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(node.getId())) {
            node.setId(UUID.randomUUID().toString());
        }
        node.setCreateUser(SessionUtils.getUserId());
        double pos = getNextLevelPos(node.getProjectId(), node.getLevel(), node.getParentId());
        node.setPos(pos);
        dataRulesNodeMapper.insertSelective(node);
        return node.getId();
    }

    public int editNode(DataRulesNode request) {
        request.setUpdateTime(System.currentTimeMillis());
        return dataRulesNodeMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        if (CollectionUtils.isEmpty(nodeIds)) {
            return 1;
        }

        // 删除所有节点下的计划
        DataRulesExample example = new DataRulesExample();
        example.createCriteria().andNodeIdIn(nodeIds);
        dataRulesMapper.deleteByExample(example);
        // 删除节点
        DataRulesNodeExample planNodeExample = new DataRulesNodeExample();
        planNodeExample.createCriteria().andIdIn(nodeIds);
        return dataRulesNodeMapper.deleteByExample(planNodeExample);
    }

    public void dragNode(DragDataRulesRequest request) {
        checkTestCaseNodeExist(request);
        List<String> nodeIds = request.getNodeIds();
        DataRulesNodeDTO nodeTree = request.getNodeTree();
        if (nodeTree == null) {
            return;
        }
        List<DataRulesNode> updateNodes = new ArrayList<>();
        buildUpdateTestCase(nodeTree, updateNodes, "0", 1);
        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());
        batchUpdateTestCaseNode(updateNodes);
    }

    @Override
    public String insertNode(String nodeName, String pId, String projectId, Integer level, String path) {
        DataRulesNode planNode = new DataRulesNode();
        planNode.setName(nodeName.trim());
        planNode.setParentId(pId);
        planNode.setProjectId(projectId);
        planNode.setCreateTime(System.currentTimeMillis());
        planNode.setUpdateTime(System.currentTimeMillis());
        planNode.setLevel(level);
        planNode.setCreateUser(SessionUtils.getUserId());
        planNode.setId(UUID.randomUUID().toString());
        double pos = getNextLevelPos(projectId, level, pId);
        planNode.setPos(pos);
        dataRulesNodeMapper.insert(planNode);
        return planNode.getId();
    }

    @Override
    public DataRulesNodeDTO getNode(String id) {
        return extDataRulesNodeMapper.getNode(id);
    }

    @Override
    public void updatePos(String id, Double pos) {
        extDataRulesNodeMapper.updatePos(id, pos);
    }

    @Override
    protected void refreshPos(String projectId, int level, String parentId) {
        List<DataRulesNode> nodes = getPos(projectId, level, parentId, "pos asc");
        if (!CollectionUtils.isEmpty(nodes)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            DataRulesNodeMapper batchMapper = sqlSession.getMapper(DataRulesNodeMapper.class);
            AtomicDouble pos = new AtomicDouble(DEFAULT_POS);
            nodes.forEach((node) -> {
                node.setPos(pos.getAndAdd(DEFAULT_POS));
                batchMapper.updateByPrimaryKey(node);
            });
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private void validateNode(DataRulesNode node) {
        checkDataRulesNodeExist(node);
    }

    private void checkDataRulesNodeExist(DataRulesNode node) {
        if (node.getName() != null) {
            DataRulesNodeExample example = new DataRulesNodeExample();
            DataRulesNodeExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName())
                    .andProjectIdEqualTo(node.getProjectId());

            if (StringUtils.isNotBlank(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andLevelEqualTo(node.getLevel());
            }

            if (StringUtils.isNotBlank(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            if (!dataRulesNodeMapper.selectByExample(example).isEmpty()) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    private double getNextLevelPos(String projectId, int level, String parentId) {
        List<DataRulesNode> list = getPos(projectId, level, parentId, "pos desc");
        if (!CollectionUtils.isEmpty(list) && list.get(0) != null && list.get(0).getPos() != null) {
            return list.get(0).getPos() + DEFAULT_POS;
        } else {
            return DEFAULT_POS;
        }
    }

    private List<DataRulesNode> getPos(String projectId, int level, String parentId, String order) {
        DataRulesNodeExample example = new DataRulesNodeExample();
        DataRulesNodeExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId).andLevelEqualTo(level);
        if (level != 1 && StringUtils.isNotBlank(parentId)) {
            criteria.andParentIdEqualTo(parentId);
        }
        example.setOrderByClause(order);
        return dataRulesNodeMapper.selectByExample(example);
    }

    private void checkTestCaseNodeExist(DataRulesNode node) {
        if (node.getName() != null) {
            DataRulesNodeExample example = new DataRulesNodeExample();
            DataRulesNodeExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName())
                    .andProjectIdEqualTo(node.getProjectId());
            if (StringUtils.isNotBlank(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andLevelEqualTo(node.getLevel());
            }

            if (StringUtils.isNotBlank(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            if (!dataRulesNodeMapper.selectByExample(example).isEmpty()) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    private void batchUpdateTestCaseNode(List<DataRulesNode> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        DataRulesNodeMapper batchMapper = sqlSession.getMapper(DataRulesNodeMapper.class);
        updateNodes.forEach(batchMapper::updateByPrimaryKeySelective);
        sqlSession.flushStatements();
        if (sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void buildUpdateTestCase(DataRulesNodeDTO rootNode,
                                     List<DataRulesNode> updateNodes, String pId, int level) {
        if (updateNodes != null) {
            DataRulesNode DataRulesNode = new DataRulesNode();
            DataRulesNode.setId(rootNode.getId());
            DataRulesNode.setLevel(level);
            DataRulesNode.setParentId(pId);
            updateNodes.add(DataRulesNode);
        }

        List<DataRulesNodeDTO> children = rootNode.getChildren();
        if (!CollectionUtils.isEmpty(children)) {
            for (DataRulesNodeDTO child : children) {
                buildUpdateTestCase(child, updateNodes, rootNode.getId(), level + 1);
            }
        }
    }

    public void checkDefaultNode(String projectId) {
        DataRulesNode defaultNode = getDefaultNode(projectId);
        if (defaultNode == null) {
            createDefaultNode(projectId);
        }
    }

    public DataRulesNode getDefaultNode(String projectId) {
        DataRulesNodeExample example = new DataRulesNodeExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(ProjectModuleDefaultNodeEnum.DEFAULT_NODE.getNodeName()).andParentIdIsNull();
        List<DataRulesNode> defaultNodes = dataRulesNodeMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(defaultNodes)) {
            return null;
        } else {
            return defaultNodes.get(0);
        }
    }

    public void createDefaultNode(String projectId) {
        // 加锁, 防止并发创建
        RLock lock = redissonClient.getLock(DATA_RULES_NODE_CREATE_KEY + ":" + projectId);
        if (lock.tryLock()) {
            try {
                // 双重检查, 判断是否已经存在默认节点
                if (getDefaultNode(projectId) != null) {
                    return;
                }

                // 创建默认节点, 只执行一次
                DataRulesNode defaultNode = new DataRulesNode();
                defaultNode.setId(UUID.randomUUID().toString());
                defaultNode.setCreateUser(SessionUtils.getUserId());
                defaultNode.setName(ProjectModuleDefaultNodeEnum.DEFAULT_NODE.getNodeName());
                defaultNode.setPos(1.0);
                defaultNode.setLevel(1);
                defaultNode.setCreateTime(System.currentTimeMillis());
                defaultNode.setUpdateTime(System.currentTimeMillis());
                defaultNode.setProjectId(projectId);
                dataRulesNodeMapper.insert(defaultNode);
            } catch (Exception e) {
                LogUtil.error(e);
            } finally {
                lock.unlock();
            }
        }
    }
}

