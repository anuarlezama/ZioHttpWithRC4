package com.alezama.app.user

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class User(name: String, age: Int)

object User: 
  given JsonEncoder[User] =
    DeriveJsonEncoder.gen[User]
  given JsonDecoder[User] =
    DeriveJsonDecoder.gen[User]
