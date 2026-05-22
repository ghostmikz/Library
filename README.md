# NMIT Library System

Java Swing library management system. Client-server architecture over TCP sockets with MySQL.

## Requirements

- **JDK 17 or later** — [Download from Adoptium](https://adoptium.net/)
- **MySQL 8** (or MariaDB 10.6+) running on port 3306
- NetBeans 18+ (if opening as a project)

## Database Setup

Run `db/schema.sql` as root (creates the database, tables, and `lib_user`), then load each procedure:

```sql
mysql -u root -p < db/schema.sql
mysql -u root -p < db/procedures/sp_lib_login.sql
-- ... repeat for all files in db/procedures/
```

Default admin login: `admin` / `123`

`schema.sql` creates a dedicated app user **`lib_user`** (password `lib1234`) that can connect from any host. The server uses this user by default — no root access needed at runtime.

## LAN Configuration

**Server machine** — edit `server.properties` before starting:
```properties
server.port=9091
db.host=localhost   # or the LAN IP of the MySQL machine
db.port=3306
db.user=lib_user
db.password=lib1234
```

**Client machines** — edit `client.properties` before starting:
```properties
server.host=192.168.1.X   # LAN IP of the server machine
server.port=9091
```

## Running in NetBeans

1. Download ZIP and extract
2. Open NetBeans → **File → Open Project**
3. Open `lib-server` folder → click Open
4. Open `lib-client` folder → click Open
5. Right-click `lib-server` → **Run** (starts server on port 9091)
6. Right-click `lib-client` → **Run** (starts the client)

## Running with scripts

**Windows** — run each in a separate command prompt:
```
run-server.bat
run-client.bat
```

**Linux / macOS:**
```
./run-server.sh
./run-client.sh
```

## Roles

| Role | Panels |
|------|--------|
| Admin | Dashboard, Books, Borrows, Librarians |
| Librarian | Dashboard, Books, Borrows |

## Features

- Book management (add, edit, delete, search, availability filter)
- Borrow tracking — record who borrowed what, due dates, overdue highlighting in red
- Mark books as returned (restores available count)
- Librarian management (admin only)
- Dashboard with live stats
- Mongolian / English language switcher (instant, no restart)
