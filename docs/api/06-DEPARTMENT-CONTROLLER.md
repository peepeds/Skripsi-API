# Department Controller Documentation

Base URL: `/department`

---

## 1. Get All Departments

**Endpoint:** `GET /department`

**Access:** Public

**Deskripsi:** Mengambil daftar semua departemen/jurusan yang tersedia.

### Request

- Method: GET
- No parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get All Department",
  "result": [
    {
      "id": 1,
      "name": "Teknik Informatika",
      "code": "TI",
      "description": "Program studi teknologi informasi"
    },
    {
      "id": 2,
      "name": "Sistem Informasi",
      "code": "SI",
      "description": "Program studi sistem informasi"
    }
  ]
}
```

---

## 2. Create Department

**Endpoint:** `POST /department`

**Access:** Admin only (`hasAnyRole('ADMIN')`)

**Deskripsi:** Membuat departemen baru.

### Request Body

```json
{
  "name": "Teknik Elektro",
  "code": "TE",
  "description": "Program studi teknik elektro"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| name | String | Ya | Nama departemen |
| code | String | Ya | Kode departemen unik |
| description | String | Tidak | Deskripsi departemen |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Create Department",
  "result": {
    "id": 3,
    "name": "Teknik Elektro",
    "code": "TE",
    "description": "Program studi teknik elektro"
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

## 3. Update Department

**Endpoint:** `PATCH /department/{id}`

**Access:** Admin only (`hasAnyRole('ADMIN')`)

**Deskripsi:** Memperbarui data departemen berdasarkan ID.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| id | Integer | ID departemen |

### Request Body

```json
{
  "name": "Teknik Elektro Updated",
  "code": "TE",
  "description": "Program studi teknik elektro terbaru"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| name | String | Ya | Nama departemen baru |
| code | String | Ya | Kode departemen baru |
| description | String | Tidak | Deskripsi baru |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Update Department",
  "result": {
    "id": 3,
    "name": "Teknik Elektro Updated",
    "code": "TE",
    "description": "Program studi teknik elektro terbaru"
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
  "message": "Department not found"
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

- **Get All Departments:** Public
- **Create Department:** Admin only
- **Update Department:** Admin only
