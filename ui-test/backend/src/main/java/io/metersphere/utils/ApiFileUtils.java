package io.metersphere.utils;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.request.BodyFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.util.List;

public class ApiFileUtils extends FileUtils {

    public static String getFilePath(BodyFile file) {
        String type = StringUtils.isNotEmpty(file.getFileType()) ? file.getFileType().toLowerCase() : null;
        String name = file.getName();
        if (type != null && !name.endsWith(type)) {
            name = StringUtils.join(name, ".", type);
        }
        return StringUtils.join(ApiFileUtils.BODY_FILE_DIR, File.separator, file.getProjectId(), File.separator, name);
    }

    public static String getFilePath(FileMetadata fileMetadata) {
        String type = StringUtils.isNotEmpty(fileMetadata.getType()) ? fileMetadata.getType().toLowerCase() : null;
        String name = fileMetadata.getName();
        if (type != null && !name.endsWith(type)) {
            name = StringUtils.join(name, ".", type);
        }
        return StringUtils.join(ApiFileUtils.BODY_FILE_DIR, File.separator, fileMetadata.getProjectId(), File.separator, name);
    }

    /**
     * 获取当前jmx 涉及到的文件
     *
     * @param tree
     */
    public static void getFiles(HashTree tree, List<BodyFile> files) {
        for (Object key : tree.keySet()) {
            HashTree node = tree.get(key);
            if (key instanceof HTTPSamplerProxy) {
                HTTPSamplerProxy source = (HTTPSamplerProxy) key;
                if (source != null && source.getHTTPFiles().length > 0) {
                    for (HTTPFileArg arg : source.getHTTPFiles()) {
                        BodyFile file = new BodyFile();
                        file.setId(arg.getParamName());
                        file.setName(arg.getPath());
                        files.add(file);
                    }
                }
            } else if (key instanceof CSVDataSet) {
                CSVDataSet source = (CSVDataSet) key;
                if (source != null && StringUtils.isNotEmpty(source.getPropertyAsString("filename"))) {
                    BodyFile file = new BodyFile();
                    file.setId(source.getPropertyAsString("filename"));
                    file.setName(source.getPropertyAsString("filename"));
                    files.add(file);
                }
            }
            if (node != null) {
                getFiles(node, files);
            }
        }
    }
}
