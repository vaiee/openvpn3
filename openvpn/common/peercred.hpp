//    OpenVPN -- An application to securely tunnel IP networks
//               over a single port, with support for SSL/TLS-based
//               session authentication and key exchange,
//               packet encryption, packet authentication, and
//               packet compression.
//
//    Copyright (C) 2012-2015 OpenVPN Technologies, Inc.
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Affero General Public License Version 3
//    as published by the Free Software Foundation.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Affero General Public License for more details.
//
//    You should have received a copy of the GNU Affero General Public License
//    along with this program in the COPYING file.
//    If not, see <http://www.gnu.org/licenses/>.

#ifndef OPENVPN_COMMON_PEERCRED_H
#define OPENVPN_COMMON_PEERCRED_H

#include <sys/types.h>
#include <sys/socket.h>

#include <openvpn/common/exception.hpp>

namespace openvpn {
  namespace SockOpt {

    struct Creds
    {
      Creds(const int uid_arg=-1, const int gid_arg=-1, const int pid_arg=-1)
	: uid(uid_arg),
	  gid(gid_arg),
	  pid(pid_arg)
      {
      }

      int uid;
      int gid;
      int pid;
    };

    // get credentials of process on other side of unix socket
    inline bool peercreds(const int fd, Creds& cr)
    {
      struct ucred uc;
      socklen_t uc_len = sizeof(uc);
      if (::getsockopt(fd, SOL_SOCKET, SO_PEERCRED, &uc, &uc_len) != 0)
	return false;
      cr = Creds(uc.uid, uc.gid, uc.pid);
      return true;
    }

  }
}

#endif