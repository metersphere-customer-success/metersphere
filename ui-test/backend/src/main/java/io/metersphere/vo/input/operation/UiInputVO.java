package io.metersphere.vo.input.operation;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.constants.StorageEnums;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.UiConfigConstants;
import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.utils.JSON;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.TemplateUtils;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Data
public class UiInputVO extends UiCommandBaseVo {

    private static FileManagerService fileManagerService;
    public static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    private String type = "Input";
    private String clazzName = UiInputVO.class.getCanonicalName();
    // 输入内容
    private String inputContent;
    private String id;
    private String projectId;
    // 是否追加输入
    private Boolean addInput;
    //关联的文件
    private List<FileMetadata> files;

    @Override
    public String getCommand(MsUiCommand command) {
        // 输入类型
        this.id = command.getId();
        this.projectId = command.getProjectId();
        if ("inputBox".equalsIgnoreCase(getOptContent())) {
            if (CollectionUtils.isNotEmpty(files)) {
                return "fileUpload";
            }
            // 可追加输入，为 sendKeys 指令
            if (addInput != null && addInput) {
                return "sendKeys";
            } else {
                // 清空输入，为 type 指令
                return "type";
            }
        } else if ("editableBox".equalsIgnoreCase(getOptContent())) {
            // 可编辑段落类型
            return "editContent";
        } else {
            return "sendKeys";
        }
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        // 输入的值
        if (StringUtils.isNotBlank(inputContent)) {
            try {
                String regx = "^[a-zA-Z]:\\\\(((?![<>:\"/\\\\|?*]).)+((?<![ .])\\\\)?)*$";
                Pattern pattern = Pattern.compile(regx);
                if (pattern.matcher(inputContent).matches()) {
                    inputContent = inputContent.replaceAll("\\\\", "/");
                }

                if (addInput == null || !addInput) {
                    // type 将转移字符移除 \n \r \\ , 然后再将\转义为 \\
                    String regx1 = "\\\\[n|r|t|\\\\]";
                    inputContent = inputContent.replaceAll(regx1, "")
                            .replaceAll("\\\\", "\\\\\\\\");
                }

            } catch (Exception e) {
            }
        } else {
            //处理文件上传
            return validateFileAndDownload();
        }
        return inputContent;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        String atomicCommand = replaceNote(sideCommand.getCommand());
        String originTarget = sideCommand.getTarget();
        UiInputVO vo = new UiInputVO();
        if (atomicCommand.equalsIgnoreCase("sendKeys") || atomicCommand.equalsIgnoreCase("type")) {
            vo.setOptContent("inputBox");
            if (atomicCommand.equalsIgnoreCase("sendKeys")) {
                vo.setAddInput(true);
            } else {
                vo.setAddInput(false);
            }
        } else {
            vo.setOptContent("editableBox");
        }
        vo.setLocatorProp(originTarget);
        vo.setInputContent(sideCommand.getValue());
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiInputVO vo = null;
        if (command.getVo() instanceof UiInputVO) {
            vo = (UiInputVO) command.getVo();
        }
        String validateStr = vo.validateLocateString();
        if (StringUtils.isNotBlank(validateStr)) {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, validateStr));
            return r;
        }
        if (StringUtils.isBlank(vo.getInputContent())) {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("input_content") + Translator.get("is_null")));
            return r;
        }
        return r;
    }

    private String validateFileAndDownload() {
        List<String> filePaths = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(this.files)) {
            try {
                this.files.forEach(
                        f -> {
                            if (StringUtils.isNotBlank(f.getId())) {
                                FileMetadata fileMetadata = CommonBeanFactory.getBean(FileMetadataService.class).getFileMetadataById(f.getId());

                                if (fileMetadata == null) {
                                    MSException.throwException(Translator.get("file_not_exists"));
                                }
                                if (!StringUtils.equalsIgnoreCase(fileMetadata.getStorage(), StorageEnums.LOCAL.name())) {
                                    if (!new File(UiConfigConstants.FILE_UPLOAD_DIR + "/" + f.getName()).exists()) {
                                        byte[] bytes = CommonBeanFactory.getBean(FileMetadataService.class).loadFileAsBytes(f.getId());
                                        if (bytes.length == 0) {
                                            MSException.throwException(Translator.get("file_not_exists"));
                                        }
                                        FileUtils.byteToFile(bytes, UiConfigConstants.FILE_UPLOAD_DIR, f.getName());
                                    }
                                    filePaths.add(UiConfigConstants.FILE_UPLOAD_DIR + "/" + f.getName());
                                } else {
                                    if (StringUtils.isEmpty(f.getPath()) || !new File(f.getPath()).exists()) {
                                        MSException.throwException(Translator.get("file_not_exists"));
                                    }
                                    filePaths.add(f.getPath());
                                }
                            }
                        }
                );
            } catch (Exception e) {
                LogUtil.error("下载文件失败!文件【" + JSON.toJSONString(this.files) + "】", e);
            }
        }
        return TemplateUtils.escapeQuotes(JSON.toJSONString(filePaths));
    }

    public static void downloadFile(String projectId, String path, String name) {
        // MinIO文件下载
        if (fileManagerService == null) {
            fileManagerService = CommonBeanFactory.getBean(FileManagerService.class);
        }
        byte[] content = fileManagerService.downloadFile(getRequest(projectId, path, name));
        if (ArrayUtils.isNotEmpty(content)) {
            FileUtils.createFile(path, content);
        }
    }

    private static FileRequest getRequest(String projectId,String path, String name) {
        FileRequest request = new FileRequest();
        request.setProjectId(projectId);
        request.setFileName(name);
        request.setStorage(StorageConstants.MINIO.name());
        LoggerUtil.info("开始从MinIO处理文件：", path);
        return request;
    }
}
