package com.alezama.app.user

import zio.http.{Request, Response}
import zio.http.*
import zio.*
import zio.http.endpoint.Endpoint
import zio.json.*
object UserApp:

  private val getUsers: Route[UserRepo, Nothing] = Method.GET / "users" ->
    handler(Handler.fromZIO(
      for {
        repo <- ZIO.service[UserRepo]
        users <- repo.users
      } yield Response.json(users.toJson)
    ))
      .mapError {
        error =>
          http.Response(
            status = Status.InternalServerError, body = Body.fromString(error.toString, Charsets.Http)
          )
      }

  private val postUsers = Method.POST / "users" ->
    handler {
      Handler.fromFunctionZIO[Request] {
        req =>
          for {
            repo <- ZIO.service[UserRepo]
            u <- req.body.asString.map(_.fromJson[User])
            r <- u match
              case Left(e) =>
                ZIO.succeed(Response.text(e).status(Status.BadRequest))
              case Right(user) =>
                repo.register(user).map(result => Response.json(result.toJson))
          } yield r
      }
    }.mapError {
    error =>
      http.Response(
        status = Status.InternalServerError, body = Body.fromString(error.toString, Charsets.Http)
      )
    }


  private val sayHello = Method.GET / "hello" ->
    handler(Handler.fromZIO(ZIO.succeed(Response.text("Everything is OK"))))
  

  // I didn't found a way to handle the not found error - left as alternative
  private val getUserByIdPost = Endpoint(Method.GET / "users" / string("userId")).out[String]

  val getUserByIdPostRoute = getUserByIdPost.implement[UserRepo] {
    Handler.fromFunctionZIO[String] {
      case id: String =>
        for {
          repo <- ZIO.service[UserRepo]
          user <- repo.lookup(id)
          r <- user match
            case Some(value) => ZIO.succeed(value.toJson)
            case None => ZIO.fail(new RuntimeException("Not found"))
        } yield r
    }.orDie
  }

  val getUserByIdPost_v2 = Method.GET / "users" / string("userId") ->
    handler {
      (userId: String, request: Request) => {
        Handler.fromFunctionZIO[(String, Request)] { (userId, req) =>
          for {
            repo <- ZIO.service[UserRepo]
            user <- repo.lookup(userId)
            r <- user match
              case Some(value) => ZIO.succeed(Response.json(value.toJson))
              case None => ZIO.succeed(Response.text("User ID not found").status(Status.NotFound))
          } yield r
        }
      }
    }.mapError {
    error =>
      http.Response(
        status = Status.InternalServerError, body = Body.fromString(error.toString, Charsets.Http)
      )
  }

  def apply(): HttpApp[UserRepo] = Routes(sayHello, getUsers, postUsers, getUserByIdPost_v2).toHttpApp





