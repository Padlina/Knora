/*
 * Copyright © 2015 Lukas Rosenthaler, Benjamin Geer, Ivan Subotic,
 * Tobias Schweizer, André Kilchenmann, and Sepideh Alassi.
 * This file is part of Knora.
 * Knora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Knora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public
 * License along with Knora.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.knora.webapi.routing.v1

import java.util.UUID

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.apache.commons.validator.routines.UrlValidator
import org.knora.webapi._
import org.knora.webapi.messages.v1.responder.usermessages._
import org.knora.webapi.routing.{Authenticator, RouteUtilV1}
import org.knora.webapi.util.InputValidation

/**
  * Provides a spray-routing function for API routes that deal with lists.
  */
object UsersRouteV1 extends Authenticator {

    /* bring json protocol into scope */
    import UserV1JsonProtocol._

    private val schemes = Array("http", "https")
    private val urlValidator = new UrlValidator(schemes)

    def knoraApiPath(_system: ActorSystem, settings: SettingsImpl, log: LoggingAdapter): Route = {


        implicit val system: ActorSystem = _system
        implicit val executionContext = system.dispatcher
        implicit val timeout = settings.defaultTimeout
        val responderManager = system.actorSelection("/user/responderManager")

        path("v1" / "users") {
            get {
                /* return all users */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)
                    val requestMessage = UsersGetRequestV1(userProfile)

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            } ~
            post {
                /* create a new user */
                entity(as[CreateUserApiRequestV1]) { apiRequest => requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val requestMessage = UserCreateRequestV1(
                        createRequest = apiRequest,
                        userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
                }
            }
        } ~
        path("v1" / "users" / Segment) { value =>
            get {
                /* return a single user identified by iri or email */
                parameters("identifier" ? "iri") { (identifier: String) =>
                    requestContext =>
                        val userProfile = getUserProfileV1(requestContext)

                        /* check if email or iri was supplied */
                        val requestMessage = if (identifier == "email") {
                            UserProfileByEmailGetRequestV1(value, UserProfileType.RESTRICTED, userProfile)
                        } else  {
                            val userIri = InputValidation.toIri(value, () => throw BadRequestException(s"Invalid user IRI $value"))
                            UserProfileByIRIGetRequestV1(userIri, UserProfileType.RESTRICTED, userProfile)
                        }
                        RouteUtilV1.runJsonRoute(
                            requestMessage,
                            requestContext,
                            settings,
                            responderManager,
                            log
                        )
                }
            } ~
            put {
                /* update a user identified by iri */
                entity(as[ChangeUserApiRequestV1]) { apiRequest => requestContext =>

                    val userIri = InputValidation.toIri(value, () => throw BadRequestException(s"Invalid user IRI $value"))
                    val userProfile = getUserProfileV1(requestContext)

                    /* the api request is already checked at time of creation. see case class. */

                    val requestMessage = if (apiRequest.oldPassword.isDefined && apiRequest.newPassword.isDefined) {
                        /* update existing user's password */
                        UserChangePasswordRequestV1(
                            userIri = userIri,
                            changeUserRequest = apiRequest,
                            userProfile,
                            apiRequestID = UUID.randomUUID()
                        )
                    } else if (apiRequest.status.isDefined) {
                        /* update existing user's status */
                        UserChangeStatusRequestV1(
                            userIri,
                            changeUserRequest = apiRequest,
                            userProfile,
                            apiRequestID = UUID.randomUUID()
                        )
                    } else if (apiRequest.systemAdmin.isDefined) {
                        /* update existing user's system admin membership status */
                        UserChangeSystemAdminMembershipStatusRequestV1(
                            userIri,
                            changeUserRequest = apiRequest,
                            userProfile,
                            apiRequestID = UUID.randomUUID()
                        )
                    } else {
                        /* update existing user's basic information */
                        UserChangeBasicUserDataRequestV1(
                            userIri,
                            changeUserRequest = apiRequest,
                            userProfile,
                            apiRequestID = UUID.randomUUID()
                        )
                    }

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
                }
            } ~ delete {
                /* delete a user identified by iri */
                requestContext => {
                    val userIri = InputValidation.toIri(value, () => throw BadRequestException(s"Invalid user IRI $value"))
                    val userProfile = getUserProfileV1(requestContext)

                    /* update existing user's status to false */
                    val requestMessage = UserChangeStatusRequestV1(
                        userIri,
                        changeUserRequest = ChangeUserApiRequestV1(status = Some(false)),
                        userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
                }
            }
        } ~
        path("v1" / "users" / "projects" / Segment) { userIri =>
            get {
                /* get user's project memberships */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val checkedUserIri = InputValidation.toIri(userIri, () => throw BadRequestException(s"Invalid user IRI $userIri"))

                    val requestMessage = UserProjectMembershipsGetRequestV1(
                        userIri = checkedUserIri,
                        userProfileV1 = userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            }
        } ~
        path("v1" / "users" / "projects" / Segment / Segment) { (userIri, projectIri) =>
            post {
                /* add user to project */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val checkedUserIri = InputValidation.toIri(userIri, () => throw BadRequestException(s"Invalid user IRI $userIri"))
                    val checkedProjectIri = InputValidation.toIri(projectIri, () => throw BadRequestException(s"Invalid project IRI $projectIri"))

                    val requestMessage = UserProjectMembershipAddRequestV1(
                        userIri = checkedUserIri,
                        projectIri = checkedProjectIri,
                        userProfileV1 = userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            } ~
            delete {
                /* remove user from project (and all groups belonging to this project) */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val checkedUserIri = InputValidation.toIri(userIri, () => throw BadRequestException(s"Invalid user IRI $userIri"))
                    val checkedProjectIri = InputValidation.toIri(projectIri, () => throw BadRequestException(s"Invalid project IRI $projectIri"))

                    val requestMessage = UserProjectMembershipRemoveRequestV1(
                        userIri = checkedUserIri,
                        projectIri = checkedProjectIri,
                        userProfileV1 = userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            }
        } ~
        path("v1" / "users" / "projects-admin" / Segment) { userIri =>
            get {
                /* get user's project admin memberships */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val checkedUserIri = InputValidation.toIri(userIri, () => throw BadRequestException(s"Invalid user IRI $userIri"))

                    val requestMessage = UserProjectAdminMembershipsGetRequestV1(
                        userIri = checkedUserIri,
                        userProfileV1 = userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            }
        } ~
        path("v1" / "users" / "projects-admin" / Segment / Segment) { (userIri, projectIri) =>
            post {
                /* add user to project admin */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val checkedUserIri = InputValidation.toIri(userIri, () => throw BadRequestException(s"Invalid user IRI $userIri"))
                    val checkedProjectIri = InputValidation.toIri(projectIri, () => throw BadRequestException(s"Invalid project IRI $projectIri"))

                    val requestMessage = UserProjectAdminMembershipAddRequestV1(
                        userIri = checkedUserIri,
                        projectIri = checkedProjectIri,
                        userProfileV1 = userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            } ~
            delete {
                /* remove user from project admin */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val checkedUserIri = InputValidation.toIri(userIri, () => throw BadRequestException(s"Invalid user IRI $userIri"))
                    val checkedProjectIri = InputValidation.toIri(projectIri, () => throw BadRequestException(s"Invalid project IRI $projectIri"))

                    val requestMessage = UserProjectAdminMembershipRemoveRequestV1(
                        userIri = checkedUserIri,
                        projectIri = checkedProjectIri,
                        userProfileV1 = userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            }
        } ~
        path("v1" / "users" / "groups" / Segment) { userIri =>
            get {
                /* get user's group memberships */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val checkedUserIri = InputValidation.toIri(userIri, () => throw BadRequestException(s"Invalid user IRI $userIri"))

                    val requestMessage = UserGroupMembershipsGetRequestV1(
                        userIri = checkedUserIri,
                        userProfileV1 = userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            }
        } ~
        path ("v1" / "users" / "groups" / Segment / Segment) { (userIri, groupIri) =>
            post {
                /* add user to group */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val checkedUserIri = InputValidation.toIri(userIri, () => throw BadRequestException(s"Invalid user IRI $userIri"))
                    val checkedGroupIri = InputValidation.toIri(groupIri, () => throw BadRequestException(s"Invalid group IRI $groupIri"))

                    val requestMessage = UserGroupMembershipAddRequestV1(
                        userIri = checkedUserIri,
                        groupIri = checkedGroupIri,
                        userProfileV1 = userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            } ~
            delete {
                /* remove user from group */
                requestContext =>
                    val userProfile = getUserProfileV1(requestContext)

                    val checkedUserIri = InputValidation.toIri(userIri, () => throw BadRequestException(s"Invalid user IRI $userIri"))
                    val checkedGroupIri = InputValidation.toIri(groupIri, () => throw BadRequestException(s"Invalid group IRI $groupIri"))

                    val requestMessage = UserGroupMembershipRemoveRequestV1(
                        userIri = checkedUserIri,
                        groupIri = checkedGroupIri,
                        userProfileV1 = userProfile,
                        apiRequestID = UUID.randomUUID()
                    )

                    RouteUtilV1.runJsonRoute(
                        requestMessage,
                        requestContext,
                        settings,
                        responderManager,
                        log
                    )
            }
        }
    }
}
