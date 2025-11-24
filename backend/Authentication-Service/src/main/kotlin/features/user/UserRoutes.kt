package com.collektar.features.user

import com.collektar.dto.UpdateDisplayNameRequest
import com.collektar.dto.UpdateEmailRequest
import com.collektar.dto.UpdateUsernameRequest
import com.collektar.features.user.service.IUserService
import com.collektar.shared.utility.userId
import com.collektar.shared.validation.Validator
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: IUserService) {
    post("/changeUsername") {
        val req = call.receive<UpdateUsernameRequest>()
        Validator.validateUsername(req.newUsername)
        val res = userService.updateUsername(call.userId, req)
        call.respond(HttpStatusCode.OK, res)
    }

    post("/changeDisplayName") {
        val req = call.receive<UpdateDisplayNameRequest>()
        Validator.validateDisplayName(req.newDisplayName)
        val res = userService.updateDisplayName(call.userId, req)
        call.respond(HttpStatusCode.OK, res)
    }

    post("/changeEmail") {
        val req = call.receive<UpdateEmailRequest>()
        Validator.validateEmail(req.newEmail)
        val res = userService.updateEmail(call.userId, req)
        call.respond(HttpStatusCode.OK, res)
    }
}