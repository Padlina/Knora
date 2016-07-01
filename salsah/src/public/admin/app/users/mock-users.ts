/*
 Copyright © 2015 Lukas Rosenthaler, Benjamin Geer, Ivan Subotic,
 Tobias Schweizer, André Kilchenmann, and André Fatton.

 This file is part of Knora.

 Knora is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published
 by the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Knora is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public
 License along with Knora.  If not, see <http://www.gnu.org/licenses/>.
 */

import {User} from "./user";

export const USERS: User[] = [
    {userId: "http://data.knora.org/users/root", userName: "root", givenName: "System", familyName: "Administrator", email: "root@example.com", phone: "123456", isSystemAdmin: true, isActiveUser: true},
    {userId: "http://data.knora.org/users/superuser", userName: "superuser", givenName: "System", familyName: "Administrator", email: "root@example.com", phone: "123456", isSystemAdmin: true, isActiveUser: true},
    {userId: "http://data.knora.org/users/normaluser", userName: "normaluser", givenName: "System", familyName: "Administrator", email: "root@example.com", phone: "123456", isSystemAdmin: false, isActiveUser: true},
    {userId: "http://data.knora.org/users/inactiveuser", userName: "inactiveuser", givenName: "System", familyName: "Administrator", email: "root@example.com", phone: "123456", isSystemAdmin: false, isActiveUser: false},
]