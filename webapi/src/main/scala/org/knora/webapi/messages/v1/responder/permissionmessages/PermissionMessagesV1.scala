package org.knora.webapi.messages.v1.responder.permissionmessages

import org.knora.webapi.{IRI, OntologyConstants}
import org.knora.webapi.messages.v1.responder.KnoraRequestV1
import org.knora.webapi.messages.v1.responder.usermessages.UserProfileV1


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Messages

/**
  * An abstract trait representing message that can be sent to `PermissionsResponderV1`.
  */
sealed trait PermissionsResponderRequestV1 extends KnoraRequestV1

/**
  * A message that requests the IRIs of all administrative permissions defined inside a project.
  * A successful response will contain a list of IRIs.
  *
  * @param projectIri the project for which the administrative permissions are queried.
  * @param userProfileV1 the user initiation the request.
  */
case class GetProjectAdministrativePermissionsV1(projectIri: IRI, userProfileV1: UserProfileV1) extends PermissionsResponderRequestV1

/**
  * A message that requests all administrative permissions attached to a group inside a project.
  * A successful response will contain an [[AdministrativePermissionV1]] object.
  *
  * @param projectIri the project to which the group belongs to.
  * @param groupIri the group for which we want to retrieve the permission object.
  * @param userProfileV1 the user initiating this request.
  */
case class GetGroupAdministrativePermissionV1(projectIri: IRI, groupIri: IRI, userProfileV1: UserProfileV1) extends PermissionsResponderRequestV1

/**
  * A message that requests an administrative permission object identified through his IRI.
  * A successful response will contain an [[AdministrativePermissionV1]] object.
  *
  * @param administrativePermissionIri the iri of the administrative permission object.
  */
case class GetAdministrativePermissionV1(administrativePermissionIri: IRI) extends PermissionsResponderRequestV1

/**
  * A message that requests all IRIs of default object access permissions defined inside a project.
  * A successful response will contain a list with IRIs of default object access permissions.
  *
  * @param projectIri the project for which the default object access permissions are queried.
  * @param userProfileV1 the user initiating this request.
  */
case class GetProjectDefaultObjectAccessPermissionsV1(projectIri: IRI, userProfileV1: UserProfileV1) extends PermissionsResponderRequestV1

/**
  * A message that requests a default object access permission object identified through his IRI.
  * A successful response will contain an [[DefaultObjectAccessPermissionV1]] object.
  *
  * @param defaultObjectAccessPermissionIri the iri of the default object access permission object.
  */
case class GetDefaultObjectAccessPermissionV1(defaultObjectAccessPermissionIri: IRI) extends PermissionsResponderRequestV1

// Permissions UserGroups
// Default Permissions on Resources and Values
// Default Permissions on UserGroups


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Components of messages

/**
  * Represents 'knora-base:AdministrativePermission'
  *
  * @param forProject the project this permission applies to.
  * @param forGroup the group this permission applies to.
  * @param resourceCreationPermissionValues a list of resource creation permission values given to members of the project/group combination.
  * @param hasRestrictedProjectResourceCreatePermission a list of resource classes to which the members of the project/group combination is restricted to create.
  * @param projectAdministrationPermissionValues a list of project administration permission value given to members of the project/group combination.
  * @param hasRestrictedProjectGroupAdminPermission a list of user groups to which the members of the project/group combination is restricted to perform administrative tasks.
  * @param ontologyAdministrationPermissionValues a list of ontology administrative permission values given to members of the project/group combination.
  */
case class AdministrativePermissionV1(forProject: IRI = OntologyConstants.KnoraBase.AllProjects,
                                      forGroup: IRI = OntologyConstants.KnoraBase.AllGroups,
                                      resourceCreationPermissionValues: Option[List[IRI]] = None,
                                      hasRestrictedProjectResourceCreatePermission: Option[List[IRI]] = None,
                                      projectAdministrationPermissionValues: Option[List[IRI]] = None,
                                      hasRestrictedProjectGroupAdminPermission: Option[List[IRI]] = None,
                                      ontologyAdministrationPermissionValues: Option[List[IRI]] = None
                                     )

/**
  * Represents 'knora-base:DefaultObjectAccessPermission'
  *
  * @param forProject
  * @param forGroup
  * @param forResourceClass
  * @param forProperty
  * @param hasDefaultChangeRightsPermission
  * @param hasDefaultDeletePermission
  * @param hasDefaultModifyPermission
  * @param hasDefaultViewPermission
  * @param hasDefaultRestrictedViewPermission
  */
case class DefaultObjectAccessPermissionV1(forProject: IRI = OntologyConstants.KnoraBase.AllProjects,
                                           forGroup: IRI = OntologyConstants.KnoraBase.AllGroups,
                                           forResourceClass: IRI = OntologyConstants.KnoraBase.AllResourceClasses,
                                           forProperty: IRI = OntologyConstants.KnoraBase.AllProperties,
                                           hasDefaultChangeRightsPermission: Option[List[IRI]] = None,
                                           hasDefaultDeletePermission: Option[List[IRI]] = None,
                                           hasDefaultModifyPermission: Option[List[IRI]] = None,
                                           hasDefaultViewPermission: Option[List[IRI]] = None,
                                           hasDefaultRestrictedViewPermission: Option[List[IRI]] = None
                                          )