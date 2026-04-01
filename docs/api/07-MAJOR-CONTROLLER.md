# Major Controller Documentation

Base URL: `/major`

---

## 1. Get All Majors

**Endpoint:** `GET /major`

**Access:** Public

**Deskripsi:** Mengambil daftar semua jurusan/major yang tersedia.

### Request

- Method: GET
- No parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "successfully Get All Major",
  "result": [
    {
      "id": 1,
      "name": "Software Engineering",
      "code": "SE",
      "department": "Teknik Informatika"
    },
    {
      "id": 2,
      "name": "Database Administration",
      "code": "DBA",
      "department": "Teknik Informatika"
    }
  ]
}
```

---

## 2. Get Major Options

**Endpoint:** `GET /major/options`

**Access:** Public

**Deskripsi:** Mengambil opsi major dalam format yang simplified untuk dropdown/select.

### Request

- Method: GET
- No parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Major",
  "result": [
    {
      "id": 1,
      "name": "Software Engineering"
    },
    {
      "id": 2,
      "name": "Database Administration"
    }
  ]
}
```

---

## 3. Create Major

**Endpoint:** `POST /major`

**Access:** Admin only (`hasAnyRole('ADMIN')`)

**Deskripsi:** Membuat major/jurusan baru.

### Request Body

```json
{
  "name": "Web Development",
  "code": "WD",
  "departmentId": 1
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| name | String | Ya | Nama major |
| code | String | Ya | Kode major unik |
| departmentId | Integer | Ya | ID departemen |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Created New Major",
  "result": {
    "id": 3,
    "name": "Web Development",
    "code": "WD",
    "department": "Teknik Informatika"
  }
}
```

### Error Response (403 Forbidden)

```json
{
  "success": false,
  "message": "Access Denied"
}
```

---

## 4. Update Major

**Endpoint:** `PATCH /major/{id}`

**Access:** Admin only (`hasAnyRole('ADMIN')`)

**Deskripsi:** Memperbarui data major berdasarkan ID.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| id | Integer | ID major |

### Request Body

```json
{
  "name": "Web Development Advanced",
  "code": "WD",
  "departmentId": 1
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| name | String | Ya | Nama major baru |
| code | String | Ya | Kode major baru |
| departmentId | Integer | Ya | ID departemen |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Updated Major",
  "result": {
    "id": 3,
    "name": "Web Development Advanced",
    "code": "WD",
    "department": "Teknik Informatika"
  }
}
```

### Error Response (403 Forbidden)

```json
{
  "success": false,
  "message": "Access Denied"
}
```

### Error Response (404 Not Found)

```json
{
  "success": false,
  "message": "Major not found"
}
```

---

## Response Format

Semua endpoint mengembalikan `WebResponse` dengan struktur:

```json
{
  "success": boolean,
  "message": string,
  "result": object | array
}
```

## Authentication

- **Get All Majors:** Public
- **Get Major Options:** Public
- **Create Major:** Admin only
- **Update Major:** Admin only
