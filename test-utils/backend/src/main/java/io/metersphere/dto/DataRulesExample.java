package io.metersphere.dto;

import java.util.ArrayList;
import java.util.List;

public class DataRulesExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DataRulesExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIsNull() {
            addCriterion("workspace_id is null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIsNotNull() {
            addCriterion("workspace_id is not null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdEqualTo(String value) {
            addCriterion("workspace_id =", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotEqualTo(String value) {
            addCriterion("workspace_id <>", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThan(String value) {
            addCriterion("workspace_id >", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThanOrEqualTo(String value) {
            addCriterion("workspace_id >=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThan(String value) {
            addCriterion("workspace_id <", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThanOrEqualTo(String value) {
            addCriterion("workspace_id <=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLike(String value) {
            addCriterion("workspace_id like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotLike(String value) {
            addCriterion("workspace_id not like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIn(List<String> values) {
            addCriterion("workspace_id in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotIn(List<String> values) {
            addCriterion("workspace_id not in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdBetween(String value1, String value2) {
            addCriterion("workspace_id between", value1, value2, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotBetween(String value1, String value2) {
            addCriterion("workspace_id not between", value1, value2, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andRuleContextIsNull() {
            addCriterion("rule_context is null");
            return (Criteria) this;
        }

        public Criteria andRuleContextIsNotNull() {
            addCriterion("rule_context is not null");
            return (Criteria) this;
        }

        public Criteria andRuleContextEqualTo(String value) {
            addCriterion("rule_context =", value, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextNotEqualTo(String value) {
            addCriterion("rule_context <>", value, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextGreaterThan(String value) {
            addCriterion("rule_context >", value, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextGreaterThanOrEqualTo(String value) {
            addCriterion("rule_context >=", value, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextLessThan(String value) {
            addCriterion("rule_context <", value, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextLessThanOrEqualTo(String value) {
            addCriterion("rule_context <=", value, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextLike(String value) {
            addCriterion("rule_context like", value, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextNotLike(String value) {
            addCriterion("rule_context not like", value, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextIn(List<String> values) {
            addCriterion("rule_context in", values, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextNotIn(List<String> values) {
            addCriterion("rule_context not in", values, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextBetween(String value1, String value2) {
            addCriterion("rule_context between", value1, value2, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andRuleContextNotBetween(String value1, String value2) {
            addCriterion("rule_context not between", value1, value2, "ruleContext");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andTestPointIsNull() {
            addCriterion("test_point is null");
            return (Criteria) this;
        }

        public Criteria andTestPointIsNotNull() {
            addCriterion("test_point is not null");
            return (Criteria) this;
        }

        public Criteria andTestPointEqualTo(String value) {
            addCriterion("test_point =", value, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointNotEqualTo(String value) {
            addCriterion("test_point <>", value, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointGreaterThan(String value) {
            addCriterion("test_point >", value, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointGreaterThanOrEqualTo(String value) {
            addCriterion("test_point >=", value, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointLessThan(String value) {
            addCriterion("test_point <", value, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointLessThanOrEqualTo(String value) {
            addCriterion("test_point <=", value, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointLike(String value) {
            addCriterion("test_point like", value, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointNotLike(String value) {
            addCriterion("test_point not like", value, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointIn(List<String> values) {
            addCriterion("test_point in", values, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointNotIn(List<String> values) {
            addCriterion("test_point not in", values, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointBetween(String value1, String value2) {
            addCriterion("test_point between", value1, value2, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTestPointNotBetween(String value1, String value2) {
            addCriterion("test_point not between", value1, value2, "testPoint");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("`type` is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("`type` is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("`type` =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("`type` <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("`type` >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("`type` >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("`type` <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("`type` <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("`type` like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("`type` not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("`type` in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("`type` not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("`type` between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("`type` not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andCaseQualityIsNull() {
            addCriterion("case_quality is null");
            return (Criteria) this;
        }

        public Criteria andCaseQualityIsNotNull() {
            addCriterion("case_quality is not null");
            return (Criteria) this;
        }

        public Criteria andCaseQualityEqualTo(String value) {
            addCriterion("case_quality =", value, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityNotEqualTo(String value) {
            addCriterion("case_quality <>", value, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityGreaterThan(String value) {
            addCriterion("case_quality >", value, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityGreaterThanOrEqualTo(String value) {
            addCriterion("case_quality >=", value, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityLessThan(String value) {
            addCriterion("case_quality <", value, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityLessThanOrEqualTo(String value) {
            addCriterion("case_quality <=", value, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityLike(String value) {
            addCriterion("case_quality like", value, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityNotLike(String value) {
            addCriterion("case_quality not like", value, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityIn(List<String> values) {
            addCriterion("case_quality in", values, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityNotIn(List<String> values) {
            addCriterion("case_quality not in", values, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityBetween(String value1, String value2) {
            addCriterion("case_quality between", value1, value2, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andCaseQualityNotBetween(String value1, String value2) {
            addCriterion("case_quality not between", value1, value2, "caseQuality");
            return (Criteria) this;
        }

        public Criteria andIterIsNull() {
            addCriterion("iter is null");
            return (Criteria) this;
        }

        public Criteria andIterIsNotNull() {
            addCriterion("iter is not null");
            return (Criteria) this;
        }

        public Criteria andIterEqualTo(Integer value) {
            addCriterion("iter =", value, "iter");
            return (Criteria) this;
        }

        public Criteria andIterNotEqualTo(Integer value) {
            addCriterion("iter <>", value, "iter");
            return (Criteria) this;
        }

        public Criteria andIterGreaterThan(Integer value) {
            addCriterion("iter >", value, "iter");
            return (Criteria) this;
        }

        public Criteria andIterGreaterThanOrEqualTo(Integer value) {
            addCriterion("iter >=", value, "iter");
            return (Criteria) this;
        }

        public Criteria andIterLessThan(Integer value) {
            addCriterion("iter <", value, "iter");
            return (Criteria) this;
        }

        public Criteria andIterLessThanOrEqualTo(Integer value) {
            addCriterion("iter <=", value, "iter");
            return (Criteria) this;
        }

        public Criteria andIterIn(List<Integer> values) {
            addCriterion("iter in", values, "iter");
            return (Criteria) this;
        }

        public Criteria andIterNotIn(List<Integer> values) {
            addCriterion("iter not in", values, "iter");
            return (Criteria) this;
        }

        public Criteria andIterBetween(Integer value1, Integer value2) {
            addCriterion("iter between", value1, value2, "iter");
            return (Criteria) this;
        }

        public Criteria andIterNotBetween(Integer value1, Integer value2) {
            addCriterion("iter not between", value1, value2, "iter");
            return (Criteria) this;
        }

        public Criteria andGenNumIsNull() {
            addCriterion("gen_num is null");
            return (Criteria) this;
        }

        public Criteria andGenNumIsNotNull() {
            addCriterion("gen_num is not null");
            return (Criteria) this;
        }

        public Criteria andGenNumEqualTo(Integer value) {
            addCriterion("gen_num =", value, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenNumNotEqualTo(Integer value) {
            addCriterion("gen_num <>", value, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenNumGreaterThan(Integer value) {
            addCriterion("gen_num >", value, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("gen_num >=", value, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenNumLessThan(Integer value) {
            addCriterion("gen_num <", value, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenNumLessThanOrEqualTo(Integer value) {
            addCriterion("gen_num <=", value, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenNumIn(List<Integer> values) {
            addCriterion("gen_num in", values, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenNumNotIn(List<Integer> values) {
            addCriterion("gen_num not in", values, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenNumBetween(Integer value1, Integer value2) {
            addCriterion("gen_num between", value1, value2, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenNumNotBetween(Integer value1, Integer value2) {
            addCriterion("gen_num not between", value1, value2, "genNum");
            return (Criteria) this;
        }

        public Criteria andGenTypeIsNull() {
            addCriterion("gen_type is null");
            return (Criteria) this;
        }

        public Criteria andGenTypeIsNotNull() {
            addCriterion("gen_type is not null");
            return (Criteria) this;
        }

        public Criteria andGenTypeEqualTo(String value) {
            addCriterion("gen_type =", value, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeNotEqualTo(String value) {
            addCriterion("gen_type <>", value, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeGreaterThan(String value) {
            addCriterion("gen_type >", value, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeGreaterThanOrEqualTo(String value) {
            addCriterion("gen_type >=", value, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeLessThan(String value) {
            addCriterion("gen_type <", value, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeLessThanOrEqualTo(String value) {
            addCriterion("gen_type <=", value, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeLike(String value) {
            addCriterion("gen_type like", value, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeNotLike(String value) {
            addCriterion("gen_type not like", value, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeIn(List<String> values) {
            addCriterion("gen_type in", values, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeNotIn(List<String> values) {
            addCriterion("gen_type not in", values, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeBetween(String value1, String value2) {
            addCriterion("gen_type between", value1, value2, "genType");
            return (Criteria) this;
        }

        public Criteria andGenTypeNotBetween(String value1, String value2) {
            addCriterion("gen_type not between", value1, value2, "genType");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentIsNull() {
            addCriterion("gen_concurrent is null");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentIsNotNull() {
            addCriterion("gen_concurrent is not null");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentEqualTo(Integer value) {
            addCriterion("gen_concurrent =", value, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentNotEqualTo(Integer value) {
            addCriterion("gen_concurrent <>", value, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentGreaterThan(Integer value) {
            addCriterion("gen_concurrent >", value, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentGreaterThanOrEqualTo(Integer value) {
            addCriterion("gen_concurrent >=", value, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentLessThan(Integer value) {
            addCriterion("gen_concurrent <", value, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentLessThanOrEqualTo(Integer value) {
            addCriterion("gen_concurrent <=", value, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentIn(List<Integer> values) {
            addCriterion("gen_concurrent in", values, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentNotIn(List<Integer> values) {
            addCriterion("gen_concurrent not in", values, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentBetween(Integer value1, Integer value2) {
            addCriterion("gen_concurrent between", value1, value2, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andGenConcurrentNotBetween(Integer value1, Integer value2) {
            addCriterion("gen_concurrent not between", value1, value2, "genConcurrent");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodIsNull() {
            addCriterion("encrypt_method is null");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodIsNotNull() {
            addCriterion("encrypt_method is not null");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodEqualTo(String value) {
            addCriterion("encrypt_method =", value, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodNotEqualTo(String value) {
            addCriterion("encrypt_method <>", value, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodGreaterThan(String value) {
            addCriterion("encrypt_method >", value, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodGreaterThanOrEqualTo(String value) {
            addCriterion("encrypt_method >=", value, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodLessThan(String value) {
            addCriterion("encrypt_method <", value, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodLessThanOrEqualTo(String value) {
            addCriterion("encrypt_method <=", value, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodLike(String value) {
            addCriterion("encrypt_method like", value, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodNotLike(String value) {
            addCriterion("encrypt_method not like", value, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodIn(List<String> values) {
            addCriterion("encrypt_method in", values, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodNotIn(List<String> values) {
            addCriterion("encrypt_method not in", values, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodBetween(String value1, String value2) {
            addCriterion("encrypt_method between", value1, value2, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andEncryptMethodNotBetween(String value1, String value2) {
            addCriterion("encrypt_method not between", value1, value2, "encryptMethod");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Long value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Long value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Long value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Long value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Long value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Long> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Long> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Long value1, Long value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Long value1, Long value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Long value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Long value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Long value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Long value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Long value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Long> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Long> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Long value1, Long value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Long value1, Long value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andCreatorIsNull() {
            addCriterion("creator is null");
            return (Criteria) this;
        }

        public Criteria andCreatorIsNotNull() {
            addCriterion("creator is not null");
            return (Criteria) this;
        }

        public Criteria andCreatorEqualTo(String value) {
            addCriterion("creator =", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotEqualTo(String value) {
            addCriterion("creator <>", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorGreaterThan(String value) {
            addCriterion("creator >", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorGreaterThanOrEqualTo(String value) {
            addCriterion("creator >=", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorLessThan(String value) {
            addCriterion("creator <", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorLessThanOrEqualTo(String value) {
            addCriterion("creator <=", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorLike(String value) {
            addCriterion("creator like", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotLike(String value) {
            addCriterion("creator not like", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorIn(List<String> values) {
            addCriterion("creator in", values, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotIn(List<String> values) {
            addCriterion("creator not in", values, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorBetween(String value1, String value2) {
            addCriterion("creator between", value1, value2, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotBetween(String value1, String value2) {
            addCriterion("creator not between", value1, value2, "creator");
            return (Criteria) this;
        }

        public Criteria andProjectIdIsNull() {
            addCriterion("project_id is null");
            return (Criteria) this;
        }

        public Criteria andProjectIdIsNotNull() {
            addCriterion("project_id is not null");
            return (Criteria) this;
        }

        public Criteria andProjectIdEqualTo(String value) {
            addCriterion("project_id =", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotEqualTo(String value) {
            addCriterion("project_id <>", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdGreaterThan(String value) {
            addCriterion("project_id >", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdGreaterThanOrEqualTo(String value) {
            addCriterion("project_id >=", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLessThan(String value) {
            addCriterion("project_id <", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLessThanOrEqualTo(String value) {
            addCriterion("project_id <=", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLike(String value) {
            addCriterion("project_id like", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotLike(String value) {
            addCriterion("project_id not like", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdIn(List<String> values) {
            addCriterion("project_id in", values, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotIn(List<String> values) {
            addCriterion("project_id not in", values, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdBetween(String value1, String value2) {
            addCriterion("project_id between", value1, value2, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotBetween(String value1, String value2) {
            addCriterion("project_id not between", value1, value2, "projectId");
            return (Criteria) this;
        }

        public Criteria andNodeIdIsNull() {
            addCriterion("node_id is null");
            return (Criteria) this;
        }

        public Criteria andNodeIdIsNotNull() {
            addCriterion("node_id is not null");
            return (Criteria) this;
        }

        public Criteria andNodeIdEqualTo(String value) {
            addCriterion("node_id =", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotEqualTo(String value) {
            addCriterion("node_id <>", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdGreaterThan(String value) {
            addCriterion("node_id >", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdGreaterThanOrEqualTo(String value) {
            addCriterion("node_id >=", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdLessThan(String value) {
            addCriterion("node_id <", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdLessThanOrEqualTo(String value) {
            addCriterion("node_id <=", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdLike(String value) {
            addCriterion("node_id like", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotLike(String value) {
            addCriterion("node_id not like", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdIn(List<String> values) {
            addCriterion("node_id in", values, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotIn(List<String> values) {
            addCriterion("node_id not in", values, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdBetween(String value1, String value2) {
            addCriterion("node_id between", value1, value2, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotBetween(String value1, String value2) {
            addCriterion("node_id not between", value1, value2, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodePathIsNull() {
            addCriterion("node_path is null");
            return (Criteria) this;
        }

        public Criteria andNodePathIsNotNull() {
            addCriterion("node_path is not null");
            return (Criteria) this;
        }

        public Criteria andNodePathEqualTo(String value) {
            addCriterion("node_path =", value, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathNotEqualTo(String value) {
            addCriterion("node_path <>", value, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathGreaterThan(String value) {
            addCriterion("node_path >", value, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathGreaterThanOrEqualTo(String value) {
            addCriterion("node_path >=", value, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathLessThan(String value) {
            addCriterion("node_path <", value, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathLessThanOrEqualTo(String value) {
            addCriterion("node_path <=", value, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathLike(String value) {
            addCriterion("node_path like", value, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathNotLike(String value) {
            addCriterion("node_path not like", value, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathIn(List<String> values) {
            addCriterion("node_path in", values, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathNotIn(List<String> values) {
            addCriterion("node_path not in", values, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathBetween(String value1, String value2) {
            addCriterion("node_path between", value1, value2, "nodePath");
            return (Criteria) this;
        }

        public Criteria andNodePathNotBetween(String value1, String value2) {
            addCriterion("node_path not between", value1, value2, "nodePath");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}