package com.alezama.app

import com.alezama.app.user.UserApp
import com.alezama.app.user.layers.PersistentUserRepo
import com.alezama.http.demo.BackendDemo.{config, configLayer, nettyConfigLayer}
import zio.http.Server
import zio.http.netty.NettyConfig
import zio.http.netty.NettyConfig.LeakDetectionLevel
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object MainApp extends ZIOAppDefault {

  private val apps = UserApp()

  private val config = Server.Config.default
    .port(8080)
    .enableRequestStreaming

  private val nettyConfig = NettyConfig.default
    .leakDetection(LeakDetectionLevel.PARANOID)
    .maxThreads(8)

  private val configLayer = ZLayer.succeed(config)
  private val nettyConfigLayer = ZLayer.succeed(nettyConfig)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = Server.serve(apps)
    .provide(configLayer, nettyConfigLayer, Server.customized, PersistentUserRepo.layer)
    .exitCode
}
