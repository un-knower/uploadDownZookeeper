package yks.com


import java.io.{File, FileInputStream, FileOutputStream}

import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.data.Stat
import org.slf4j.LoggerFactory
import scala.language.postfixOps


/**
  * Created by cgt on 17-8-9.
  * 实现文件及文件夹的批量上传和下载
  */
object FolderUpDownUtl {
  lazy val logger = LoggerFactory.getLogger(this.getClass)
  var zkClient: CuratorFramework = _
  /**
    * 初始化CuratorFramework
    */
  def init(): Unit = {
    val retryPolicy = new ExponentialBackoffRetry(yks.com.BasicConf.BASE_SlEEP_TIMESMS,
      yks.com.BasicConf.MAX_RETRIES)
    val builder = CuratorFrameworkFactory.builder().connectString(yks.com.BasicConf.ZK_HOST).
      namespace(yks.com.BasicConf.NAME_SPACE).retryPolicy(retryPolicy).
      connectionTimeoutMs(yks.com.BasicConf.ZK_TIMEOUT)
    //使用上面的设置，构建一个CuratorFramework
    zkClient = builder.build()
    zkClient.start()
  }

  /**
    * 删除zookeeper节点下的所有文件
    */
  def fileDelete(fileName:String):Unit = {
    val newFileName = "/" + s"$fileName"
   // val newFileName =  "/" + s"$fileName"
    //val  state = zkClient.checkExists().forPath(newFileName)
   // println("zookeeper中不存在znode===:"+state)

    //if (zkClient.checkExists().forPath(newFileName) != null){
      zkClient.delete().deletingChildrenIfNeeded().forPath(newFileName)
   // }else
     // println("zookeeper===:"+newFileName)
  }

  /**
    * 销毁CuratorFramework
    */
  def destroy(): Unit = {
    zkClient.close()
  }

  /**
    *
    * @param path 绝对路径
    * @return 字节码数组
    */
  private def file2Byte(path: String) = {
    val file = new File(path)
    if (file.isDirectory) {
      null
    } else if (file.isFile) {
      val isSteam = new FileInputStream(file)
      Iterator continually isSteam.read takeWhile (-1 !=) map (_.toByte) toArray
    } else {
      throw new Exception()
    }
  }

  /**
    * 上传方法
    * @param confName 配置文件名
    * @param osPath 系统路径
    */
  def upload(confName: String, osPath: String): Unit = {
    //如果是文件
    if (new File(osPath).isFile) {
      val newConfName = s"/$confName"
      //节点不存在，且非
      if (zkClient.checkExists().forPath(newConfName)==null) {
        zkClient.create().forPath(newConfName, file2Byte(osPath))
        logger.info(s"upload $osPath,create  node:$newConfName")

      }else{
        zkClient.setData().forPath(newConfName, file2Byte(osPath))
        logger.info(s"upload $osPath,update  node:$newConfName")
      }
      return
    } else{
      //如果是文件夹
      val newConfName = s"/$confName"
      //文件夹不存在，则创建
      if (zkClient.checkExists().forPath(newConfName)==null) {
        zkClient.create().forPath(newConfName)
        logger.info(s"create node:$newConfName")
        val files = new File(osPath).listFiles()
        files.foreach { file =>
          if (file.isDirectory) {
            val childName = s"$confName/${file.getName}"
            upload(childName, file.getAbsolutePath)
          } else {
            val childName = s"/$confName/${file.getName}"
            zkClient.create().forPath(childName, file2Byte(file.getAbsolutePath))
            logger.info(s"upload ${file.getAbsolutePath} create node:$childName")
          }

        }
      }else{//znod表示的是文件夹，且已经存在，更新znod

        logger.info(s"update　directory node:$newConfName")
        val files = new File(osPath).listFiles()
        files.foreach { file =>
            val childName = s"$confName/${file.getName}"
            upload(childName, file.getAbsolutePath)
        }

      }


    }
  }


  /**
    * 下载方法
    * @param confName 配置文件名
    * @param osPath 系统路径
    */
  def download(confName: String, osPath: String): Unit = {
    if (zkClient.getChildren.forPath(confName).isEmpty) {
      val file = new File(osPath)
      val fos = new FileOutputStream(file)
      val bytes = zkClient.getData.forPath(confName)
      fos.write(bytes)
      fos.flush()
      fos.close()
      logger.info(s"download $confName,create file:$osPath")
    } else {
      new File(osPath).mkdir()
      val list = zkClient.getChildren.forPath(confName).toArray
      if (list.nonEmpty) {
        list.foreach { zkFile =>
          if (zkClient.getChildren.forPath(s"$confName/$zkFile").isEmpty) {
            val file = new File(s"$osPath/$zkFile")
            val fos = new FileOutputStream(file)
            val bytes = zkClient.getData.forPath(s"$confName/$zkFile")
            fos.write(bytes)
            fos.flush()
            fos.close()
            logger.info(s"download $confName/$zkFile,create file:$osPath/$zkFile")
          } else {
            download(s"$confName/$zkFile", s"$osPath/$zkFile")
          }
        }
      }
    }

  }


}
