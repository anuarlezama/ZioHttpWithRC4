package com.alezama.http.demo

import zio.*
import zio.http.*
import zio.http.endpoint.*
import zio.http.netty.NettyConfig
import zio.http.netty.NettyConfig.LeakDetectionLevel
import zio.stream.ZStream

import java.util.concurrent.TimeUnit

object BackendDemo extends ZIOAppDefault {

  final case class  ExampleResponse (name: String, age: Int)

  private val zioHandlerV2 = Handler.fromFunctionZIO[Request] {
    request =>
      val queryParams = request.url.queryParams.map
      ZIO.succeed(Response.text(s"The query param was ${queryParams("name").mkString(" ")}"))
  }

  private val anotherRoute =
    Method.GET / "another" -> handler(zioHandlerV2)

  private val zioHandler = Handler.fromFunctionZIO[Request] {
    request =>
      if (request.method == Method.POST)
        ZIO.fail("This request is not allowed")
      else
      Random.nextString(10).map(s => Response.text(s"$s + ${request.method}"))
  }

  val message = Chunk.fromArray("Hello world !\r\n".getBytes(Charsets.Http))

  private val textRoute =
    Method.GET / "fruits" / "a" -> handler(ZIO.succeed(Response.text("Hello World!")))

  private val streamRoute =
    Method.GET / "stream" -> handler(http.Response(
      status = Status.Ok, body = Body.fromStream(ZStream.fromChunk(message))
    ))

  private val zHandlerRoute =
    Method.GET / "zhandler" -> handler(zioHandler).mapError {
      error => http.Response(
        status = Status.InternalServerError, body = Body.fromString(error, Charsets.Http)
      )
    }



  val serverTime = Middleware.patchZIO(_ =>
    for {
      currentMilliseconds <- Clock.currentTime(TimeUnit.MILLISECONDS)
      header = Response.Patch.addHeader("X-Time", currentMilliseconds.toString)
    } yield header
  )
  val basicAuth = Middleware.basicAuthZIO {
    case Credentials(uname, upassword) if uname == "anuar" => ZIO.succeed(true)
    case _ => ZIO.succeed(false)
  }

  private val config = Server.Config.default
    .port(8080)
    .enableRequestStreaming

  private val nettyConfig = NettyConfig.default
    .leakDetection(LeakDetectionLevel.PARANOID)
    .maxThreads(8)

  private val configLayer = ZLayer.succeed(config)
  private val nettyConfigLayer = ZLayer.succeed(nettyConfig)


  val app = Routes(textRoute, zHandlerRoute, anotherRoute).toHttpApp
  val appWithAuth = Routes(streamRoute).toHttpApp @@ basicAuth

  // @@ middleware operator
  val middlewares = Middleware.requestLogging() ++ serverTime



  override def run = Server.serve((app ++ appWithAuth) @@ middlewares)
    .provide(configLayer, nettyConfigLayer, Server.customized)
    .exitCode

}
