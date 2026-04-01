# Region Controller Documentation

Base URL: `/region`

---

## 1. Get All Regions

**Endpoint:** `GET /region`

**Access:** Public

**Deskripsi:** Mengambil daftar semua region/kampus yang tersedia.

### Request

- Method: GET
- No parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get All Regions Data",
  "result": [
    {
      "id": 1,
      "name": "Jakarta",
      "provinsi": "DKI Jakarta",
      "code": "JKT"
    },
    {
      "id": 2,
      "name": "Bandung",
      "provinsi": "Jawa Barat",
      "code": "BDG"
    }
  ]
}
```

---

## 2. Get Region Options

**Endpoint:** `GET /region/options`

**Access:** Public

**Deskripsi:** Mengambil opsi region dalam format yang simplied untuk dropdown/select.

### Request

- Method: GET
- No parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Regions",
  "result": [
    {
      "id": 1,
      "name": "Jakarta"
    },
    {
      "id": 2,
      "name": "Bandung"
    }
  ]
}
```

---

## 3. Create Region

**Endpoint:** `POST /region`

**Access:** Admin only (`hasAnyRole('ADMIN')`)

**Deskripsi:** Membuat region baru.

### Request Body

```json
{
  "name": "Surabaya",
  "provinsi": "Jawa Timur",
  "code": "SBY"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| name | String | Ya | Nama region |
| provinsi | String | Ya | Provinsi |
| code | String | Ya | Kode region unik |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Created new Region",
  "result": {
    "id": 3,
    "name": "Surabaya",
    "provinsi": "Jawa Timur",
    "code": "SBY"
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

## 4. Update Region

**Endpoint:** `PATCH /region/{id}`

**Access:** Admin only (`hasAnyRole('ADMIN')`)

**Deskripsi:** Memperbarui data region berdasarkan ID.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| id | Integer | ID region |

### Request Body

```json
{
  "name": "Surabaya Updated",
  "provinsi": "Jawa Timur",
  "code": "SBY"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| name | String | Ya | Nama region baru |
| provinsi | String | Ya | Provinsi baru |
| code | String | Ya | Kode region baru |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Updated Region",
  "result": {
    "id": 3,
    "name": "Surabaya Updated",
    "provinsi": "Jawa Timur",
    "code": "SBY"
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
  "message": "Region not found"
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

- **Get All Regions:** Public
- **Get Region Options:** Public
- **Create Region:** Admin only
- **Update Region:** Admin only
