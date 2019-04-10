package world.oasis;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {

    private static final char _separator = '/';
    private static final char _dot = '.';
    private static final String _fbx_file_type = "fbx";
    private static final String _zip_file_type = ".zip";

    private static final String _model_directory = "/model";
    private static final String _output_directory = "/output";
    private static final String _temp_directory = "/temp";
    private static final String _log_directory = "/logs";
    private static final String _problematic_directory = "/problematic";

    private static String logPath = "/log";
    private static String problematicPath;

    public static void main(String[] args) {
        String currentDirectoryPath = System.getProperty("user.dir");
        // String currentDirectoryPath = "D:\\Data\\job\\模型\\FBX文件更名工具";
        String modelDirectoryPath = currentDirectoryPath + _model_directory;
        String outputDirectoryPath = currentDirectoryPath + _output_directory;
        String tempDirectoryPath = currentDirectoryPath + _temp_directory;
        String logDirectoryPath = currentDirectoryPath + _log_directory;
        String problematicDirectoryPath = currentDirectoryPath + _problematic_directory;
        logPath = currentDirectoryPath + logPath;
        problematicPath = currentDirectoryPath + _problematic_directory;

        initLog(logDirectoryPath);
        handler(modelDirectoryPath, outputDirectoryPath, tempDirectoryPath, problematicDirectoryPath);
    }

    private static void initLog(String logDirectoryPath){
        File logDirectory = new File(logDirectoryPath);
        if(!logDirectory.exists() || !logDirectory.isDirectory()){
            logDirectory.mkdirs();
        }

        File log = new File(logPath);
        if(log.exists()){
            File bak = new File(logDirectoryPath, _log_directory + "_" + getTimeStamp(false));
            try(FileInputStream fis = new FileInputStream(log);
                BufferedInputStream bis = new BufferedInputStream(fis);
                FileOutputStream fos = new FileOutputStream(bak);
                BufferedOutputStream bos = new BufferedOutputStream(fos)){
                byte[] buf = new byte[1024 * 1024];
                int len;
                while((len = bis.read(buf)) != -1){
                    bos.write(buf, 0, len);
                }
                bos.flush();
            } catch (FileNotFoundException e) {
                logException(e);
            } catch (IOException e) {
                logException(e);
            }
        }

        try(FileOutputStream fos = new FileOutputStream(log, false);
            BufferedOutputStream bos = new BufferedOutputStream(fos)){
            bos.write(getTimeStamp(true).getBytes());
            bos.write("转换任务开始".getBytes(Charset.forName("utf8")));
            bos.flush();
        } catch (FileNotFoundException e) {
            logException(e);
        } catch (IOException e) {
            logException(e);
        }
    }

    private static void handler(String modelDirectoryPath, String outputDirectoryPath, String tempDirectoryPath, String problematicDirectoryPath){
        File modelDirectory = new File(modelDirectoryPath);
        if(!modelDirectory.exists() || !modelDirectory.isDirectory()){
            logInfo("不存在模型目录，已重新生成");
            modelDirectory.mkdirs();
            return;
        }

        File outputDirectory = new File(outputDirectoryPath);
        if(!outputDirectory.exists() || !outputDirectory.isDirectory()){
            outputDirectory.mkdirs();
        }
        if(outputDirectory.listFiles().length > 0){
            clearDirectory(outputDirectory);
        }

        File problematicDirectory = new File(problematicDirectoryPath);
        if(!problematicDirectory.exists() || !problematicDirectory.isDirectory()){
            problematicDirectory.mkdirs();
        }
        if(problematicDirectory.listFiles().length > 0){
            clearDirectory(problematicDirectory);
        }

        File tempDirectory = new File(tempDirectoryPath);
        if(!tempDirectory.exists() || !tempDirectory.isDirectory()){
            tempDirectory.mkdirs();
        }
        if(tempDirectory.listFiles().length > 0){
            clearDirectory(tempDirectory);
        }

        for(File file : modelDirectory.listFiles()){
            if(!file.getName().endsWith(_zip_file_type)){
                continue;
            }

            String modelName = file.getName().replace(_zip_file_type, "");
            String modelFilePath = file.getPath();
            String tmpPath = tempDirectoryPath + _separator + modelName;
            String outputPath = outputDirectoryPath + _separator + file.getName();

            try {
                unzip(file, tmpPath);
                renameDotFBX(tmpPath, modelName);
                zipDirectory(tmpPath, outputPath);
                System.out.println("处理文件完成：" + modelFilePath);
                logInfo("处理文件完成：" + modelFilePath);
            } catch (IOException e){
                System.out.println("处理文件失败：" + modelFilePath + ", 异常信息：" + e.getMessage());
                logInfo("处理文件失败：" + modelFilePath + ", 异常信息：" + e.getMessage());
                exceptionHandler(modelFilePath, tmpPath, outputPath);
                logException(e);
                continue;
            } catch (RuntimeException e){
                System.out.println("处理文件失败：" + modelFilePath + ", 异常信息：" + e.getMessage());
                logInfo("处理文件失败：" + modelFilePath + ", 异常信息：" + e.getMessage());
                exceptionHandler(modelFilePath, tmpPath, outputPath);
                logException(e);
                continue;
            } catch (Exception e){
                System.out.println("处理文件失败：" + modelFilePath + ", 异常信息：" + e.getMessage());
                logInfo("处理文件失败：" + modelFilePath + ", 异常信息：" + e.getMessage());
                exceptionHandler(modelFilePath, tmpPath, outputPath);
                logException(e);
                continue;
            }
        }
    }

    private static void exceptionHandler(String modelPath, String tmpPath, String outputPath){
        File tmpDirectory = new File(tmpPath);
        if(tmpDirectory.exists()){
            if(tmpDirectory.listFiles().length > 0){
                clearDirectory(tmpDirectory);
            }

            tmpDirectory.delete();
        }

        File outputFile = new File(outputPath);
        if(outputFile.exists()){
            outputFile.delete();
        }

        File modelFile = new File(modelPath);
        File problematicModelFile = new File(problematicPath, modelFile.getName());
        int len;
        byte[] buf = new byte[1024 * 1024];
        try(FileInputStream fis = new FileInputStream(modelFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            FileOutputStream fos = new FileOutputStream(problematicModelFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos)){
            while ((len = bis.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            bos.flush();

        } catch (FileNotFoundException e) {
            System.out.println("复制问题模型文件失败：" + modelPath + ", 异常信息：" + e.getMessage());
            logInfo("复制问题模型文件失败：" + modelPath + ", 异常信息：" + e.getMessage());
            logException(e);
        } catch (IOException e) {
            System.out.println("复制问题模型文件失败：" + modelPath + ", 异常信息：" + e.getMessage());
            logInfo("复制问题模型文件失败：" + modelPath + ", 异常信息：" + e.getMessage());
            logException(e);
        }
    }

    private static void clearDirectory(File directory){
        for(File file : directory.listFiles()){
            if(file.isDirectory()){
                clearDirectory(file);
            }

            file.delete();
        }
    }

    private static void unzip(File sourceFile, String targetDirectoryPath) throws IOException {
        File targetDirectory = new File(targetDirectoryPath);
        if(!targetDirectory.exists() || !targetDirectory.isDirectory()){
            targetDirectory.mkdirs();
        }

        ZipEntry zipEntry;
        try(FileInputStream fis = new FileInputStream(sourceFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis, Charset.forName("utf8"))){
            while((zipEntry = zis.getNextEntry()) != null){
                int targetFileNameIndex = zipEntry.getName().indexOf(_separator) + 1;
                File file = new File(targetDirectoryPath, zipEntry.getName().substring(targetFileNameIndex));
                if(zipEntry.isDirectory()){
                    if(!file.exists()){
                        file.mkdirs();
                    }
                } else {
                    File parentFile = file.getParentFile();
                    if (parentFile != null && !parentFile.exists()){
                        parentFile.mkdirs();
                    }
                    int len;
                    byte[] buf = new byte[1024 * 1024];
                    try(FileOutputStream fos = new FileOutputStream(file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos)){
                        while ((len = zis.read(buf)) != -1) {
                            bos.write(buf, 0, len);
                        }
                        bos.flush();
                    }
                }

                zis.closeEntry();
            }
        }
    }

    private static void zipDirectory(String sourceFilePath, String targetFilePath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        if(!sourceFile.exists()){
            System.out.println("待压缩文件不存在: " + sourceFilePath);
            logInfo("待压缩文件不存在: " + sourceFilePath);
            return;
        }

        File targetFile = new File(targetFilePath);
        if(targetFile.exists()){
            System.out.println("目标压缩文件已存在: " + targetFilePath);
            logInfo("目标压缩文件已存在: " + targetFilePath);
        }

        File[] sourceFiles = sourceFile.listFiles();
        if(sourceFiles != null && sourceFiles.length > 0){
            try(FileOutputStream fos = new FileOutputStream(targetFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ZipOutputStream zos = new ZipOutputStream(bos, Charset.forName("utf8"))){
                zip(zos, sourceFile, sourceFile.getParent());
                zos.flush();
            }
        }

        sourceFile.delete();
    }

    private static void zip(ZipOutputStream zos, File sourceFile, String basePath) throws IOException{
        String zipEntryName = sourceFile.getPath().replace(basePath, "");
        if(sourceFile.isDirectory()){
            ZipEntry zipEntry = new ZipEntry(zipEntryName + _separator);
            zos.putNextEntry(zipEntry);
            for(File innerFile : sourceFile.listFiles()){
                zip(zos, innerFile, basePath);
            }
        } else {
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            zos.putNextEntry(zipEntry);
            byte[] buf = new byte[1024 * 1024];
            int len;
            try(FileInputStream fis = new FileInputStream(sourceFile);
                BufferedInputStream bis = new BufferedInputStream(fis)){
                while((len = bis.read(buf, 0, 1024 * 1024)) != -1){
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
            }
        }

        sourceFile.delete();
    }

    private static void renameDotFBX(String directoryPath, String fbxFileName){
        File directory = new File(directoryPath);
        if(!directory.exists() || !directory.isDirectory()){
            return;
        }

        File[] files = directory.listFiles();
        int fbxFileNumber = 0;
        for(File file : files){
            if(file.isDirectory()){
                if(file.getName().equals("_CustomAvatar")){
                    renameDotFBX(file.getPath(), fbxFileName);
                } else {
                    // renameDotFBX(file.getPath(), fbxFileName + "@" + file.getName());
                    renameDotFBX(file.getPath(), fbxFileName);
                }
                continue;
            }

            if(file.getName().length() < 4){
                continue;
            }

            String fileType = file.getName().substring(file.getName().length() - 3);
            if(_fbx_file_type.equalsIgnoreCase(fileType) && file.getName().charAt(file.getName().length() - 4) == _dot){
                if(++fbxFileNumber > 1){
                    throw new RuntimeException("文件夹下存在多个fbx文件");
                }
                file.renameTo(new File(file.getParentFile().getPath(), fbxFileName + _dot + _fbx_file_type));
            }

        }
    }

    private static void logInfo(String line){
        if(logPath == null || logPath.isEmpty()){
            return;
        }

        File log = new File(logPath);
        try(FileOutputStream fos = new FileOutputStream(log, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos)){
            bos.write("\r\n".getBytes());
            bos.write(getTimeStamp(true).getBytes());
            bos.write(line.getBytes(Charset.forName("utf8")));
            bos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logException(Exception exception){
        if(logPath == null || logPath.isEmpty()){
            return;
        }

        File log = new File(logPath);
        try(FileOutputStream fos = new FileOutputStream(log, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            PrintStream ps = new PrintStream(bos, true)){
            bos.write("\r\n".getBytes());
            bos.write(getTimeStamp(true).getBytes());
            bos.flush();
            exception.printStackTrace(ps);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTimeStamp(boolean isLog){
        LocalDateTime now = LocalDateTime.now();
        StringBuilder timestamp = new StringBuilder(32);
        timestamp.append(now.getYear());
        if(isLog){
            timestamp.append("-");
        }
        timestamp.append(now.getMonthValue() < 10 ? "0" + now.getMonthValue() : now.getMonthValue());
        if(isLog){
            timestamp.append("-");
        }
        timestamp.append(now.getDayOfMonth() < 10 ? "0" + now.getDayOfMonth() : now.getDayOfMonth());
        if(isLog){
            timestamp.append(" ");
        }
        timestamp.append(now.getHour() < 10 ? "0" + now.getHour() : now.getHour());
        if(isLog){
            timestamp.append(":");
        }
        timestamp.append(now.getMinute() < 10 ? "0" + now.getMinute() : now.getMinute());
        if(isLog){
            timestamp.append(":");
        }
        timestamp.append(now.getSecond() < 10 ? "0" + now.getSecond() : now.getSecond());
        if(isLog){
            timestamp.append("\t");
        }

        return timestamp.toString();
    }
}
