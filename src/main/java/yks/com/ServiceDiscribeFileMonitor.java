package yks.com;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;

/**
 * Created by cgt on 17-8-9.
 */
public class ServiceDiscribeFileMonitor {

    FileAlterationMonitor monitor = null;
    public ServiceDiscribeFileMonitor(long interval) throws Exception {
        monitor = new FileAlterationMonitor(interval);
    }

    public void monitor(String path, FileAlterationListener listener) {
        FileAlterationObserver observer = new FileAlterationObserver(new File(path));
        monitor.addObserver(observer);
        observer.addListener(listener);
    }
    public void stop() throws Exception{
        monitor.stop();
    }
    //zookeeper初始化,并上传数据到zookeeper的znode,并对文件夹进行监控
    public void start() throws Exception {
        System.out.println("-------------onStart==================");
        FolderUpDownUtl.init();
        FolderUpDownUtl.upload("testfile","/opt/servicemix/conf");
        FolderUpDownUtl.download("/testfile","/home/cgt/newfile");
        monitor.start();
    }
    public static void main(String[] args) throws Exception {
        ServiceDiscribeFileMonitor serviceMonitor = new ServiceDiscribeFileMonitor(3000);
        serviceMonitor.monitor("/opt/servicemix/conf",new ServiceDiscribeFileListener());
        serviceMonitor.start();
    }
}
