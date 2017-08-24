package yks.com;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;

/**
 * Created by cgt on 17-8-9.
 * 监听文件夹及其子文件及文件夹，当某一个文件或者文件夹发生变化，删除所有文件，重新下载文件
 */
public class ServiceDiscribeFileListener implements FileAlterationListener {

    ServiceDiscribeFileMonitor monitor = null;

    public void commondeal() {
        boolean isDeleteSucc = true;
        if(new File("/home/cgt/newfile").exists()){
             isDeleteSucc = FileUtils.deleteQuietly(new File("/home/cgt/newfile"));
        }
        if(isDeleteSucc){
            FolderUpDownUtl.upload("testfile","/opt/servicemix/conf");
            FolderUpDownUtl.download("/testfile","/home/cgt/newfile");
        }else
            System.out.println("/home/cgt/newfile删除失败");

        System.out.println("调试－－＝＝＝＝＝＝＝");
    }

    public void onStart(FileAlterationObserver observer) {
        System.out.println("onStart============ServiceDiscribeFileListener======");
    }

    public void onDirectoryCreate(File directory) {
        System.out.println("onDirectoryCreate:" +  directory.getName());
        commondeal();
    }


    public void onDirectoryChange(File directory) {
        System.out.println("onDirectoryChange:" + directory.getName());
        commondeal();
    }


    public void onDirectoryDelete(File directory) {
        System.out.println("onDirectoryDelete:" + directory.getName());
        FolderUpDownUtl.fileDelete("testfile");
        commondeal();
    }


    public void onFileCreate(File file) {
        System.out.println("onFileCreate:" + file.getName());
        commondeal();
    }


    public void onFileChange(File file) {
        System.out.println("onFileChange : " + file.getName());
        commondeal();
    }


    public void onFileDelete(File file) {
        System.out.println("onFileDelete :" + file.getName());
        FolderUpDownUtl.fileDelete("testfile");
        commondeal();
    }


    public void onStop(FileAlterationObserver observer) {
        System.out.println("onStop============");
    }
}
