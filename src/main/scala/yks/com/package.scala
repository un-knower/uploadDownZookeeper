package yks.com

/**
  * Created by cgt on 17-8-9.
  */
package object BasicConf {
  //zookeeper主机端口
  val ZK_HOST = "192.168.45.3:2181"
  //超时时间
  val ZK_TIMEOUT = 10000
  //zk命名空间
  val NAME_SPACE = "test18"
  //初始化重试时间
  val BASE_SlEEP_TIMESMS = 3
  //重试次数
  val MAX_RETRIES = 100

}
