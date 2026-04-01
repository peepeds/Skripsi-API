# Company Controller Documentation

Base URL: `/company`

---

## 1. Get All Companies (with Search)

**Endpoint:** `GET /company`

**Access:** Public

**Deskripsi:** Mengambil daftar perusahaan dengan dukungan pencarian dan pagination.

### Query Parameters

| Parameter | Type | Required | Default | Deskripsi |
|-----------|------|----------|---------|-----------|
| search | String | Tidak | - | Kata kunci pencarian perusahaan |
| cursor | Long | Tidak | - | Cursor untuk pagination |
| limit | Integer | Tidak | 15 | Jumlah data per halaman |

### Success Response - Without Search (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Companies data",
  "result": [
    {
      "id": 1,
      "name": "TechCorp Indonesia",
      "slug": "techcorp-indonesia",
      "description": "Perusahaan teknologi terdepan",
      "logo": "https://minio.example.com/logo.png",
      "rating": 4.5,
      "reviewCount": 120
    }
  ],
  "meta": {
    "nextCursor": 2,
    "hasMore": true,
    "limit": 15
  }
}
```

### Success Response - With Search (200 OK)

```json
{
  "success": true,
  "message": "Successfully search companies",
  "result": [
    {
      "id": 1,
      "name": "TechCorp Indonesia",
      "slug": "techcorp-indonesia",
      "description": "Perusahaan teknologi terdepan",
      "logo": "https://minio.example.com/logo.png"
    }
  ]
}
```

---

## 2. Submit Company Request

**Endpoint:** `POST /company/requests`

**Access:** Authenticated users (`isAuthenticated()`)

**Deskripsi:** Mengajukan permintaan untuk menambahkan perusahaan baru.

### Request Body

```json
{
  "companyName": "New Tech Start-up",
  "industry": "Software Development",
  "description": "Kami adalah startup teknologi yang inovatif",
  "website": "https://newtech.example.com",
  "contactEmail": "contact@newtech.example.com",
  "phone": "021-1234567"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| companyName | String | Ya | Nama perusahaan |
| industry | String | Ya | Industri perusahaan |
| description | String | Ya | Deskripsi perusahaan |
| website | String | Tidak | Website perusahaan |
| contactEmail | String | Ya | Email kontak |
| phone | String | Ya | Nomor telepon |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully submit company request",
  "result": {
    "id": 1,
    "status": "PENDING",
    "companyName": "New Tech Start-up",
    "submittedAt": "2024-02-01T10:30:00Z"
  }
}
```

### Error Response (401 Unauthorized)

```json
{
  "success": false,
  "message": "Unauthorized"
}
```

---

## 3. Get Company Requests

**Endpoint:** `GET /company/requests`

**Access:** Admin only (`hasRole('ADMIN')`)

**Deskripsi:** Mengambil daftar semua permintaan perusahaan dengan filter status.

### Query Parameters

| Parameter | Type | Required | Default | Deskripsi |
|-----------|------|----------|---------|-----------|
| status | String | Tidak | - | Status filter (PENDING/APPROVED/REJECTED) |
| cursor | Long | Tidak | - | Cursor untuk pagination |
| limit | Integer | Tidak | 15 | Jumlah data per halaman |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully get company requests",
  "result": [
    {
      "id": 1,
      "status": "PENDING",
      "companyName": "New Tech Start-up",
      "submittedBy": "john@example.com",
      "submittedAt": "2024-02-01T10:30:00Z"
    }
  ],
  "meta": {
    "nextCursor": 2,
    "hasMore": true,
    "limit": 15
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

## 4. Review Company Request

**Endpoint:** `PATCH /company/requests/{requestId}/review`

**Access:** Admin only (`hasRole('ADMIN')`)

**Deskripsi:** Admin mereview dan menyetujui atau menolak permintaan perusahaan.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| requestId | Long | ID dari company request |

### Request Body

```json
{
  "status": "APPROVED",
  "notes": "Perusahaan valid dan sudah diverifikasi"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| status | String | Ya | Status review (APPROVED/REJECTED) |
| notes | String | Tidak | Catatan review |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully review company request",
  "result": {
    "id": 1,
    "status": "APPROVED",
    "companyName": "New Tech Start-up",
    "notes": "Perusahaan valid dan sudah diverifikasi",
    "reviewedAt": "2024-02-01T11:00:00Z"
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

## 5. Get Company Request Detail

**Endpoint:** `GET /company/request/{requestId}`

**Access:** Authenticated users (`isAuthenticated()`)

**Deskripsi:** Mengambil detail lengkap dari permintaan perusahaan tertentu.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| requestId | Long | ID dari company request |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Company request detail",
  "result": {
    "id": 1,
    "status": "PENDING",
    "companyName": "New Tech Start-up",
    "industry": "Software Development",
    "description": "Kami adalah startup teknologi yang inovatif",
    "website": "https://newtech.example.com",
    "contactEmail": "contact@newtech.example.com",
    "phone": "021-1234567",
    "submittedBy": "john@example.com",
    "submittedAt": "2024-02-01T10:30:00Z",
    "reviewNotes": null
  }
}
```

### Error Response (404 Not Found)

```json
{
  "success": false,
  "message": "Company request not found"
}
```

---

## 6. Get Top Companies by Rating

**Endpoint:** `GET /company/top-ratings`

**Access:** Public

**Deskripsi:** Mengambil 10 perusahaan teratas berdasarkan rating.

### Request

- Method: GET
- No parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully get top 10 companies by rating",
  "result": [
    {
      "id": 1,
      "name": "TechCorp Indonesia",
      "slug": "techcorp-indonesia",
      "logo": "https://minio.example.com/logo.png",
      "rating": 4.8,
      "reviewCount": 250
    }
  ]
}
```

---

## 7. Get Company by Slug

**Endpoint:** `GET /company/{slug}`

**Access:** Public

**Deskripsi:** Mengambil profil lengkap perusahaan berdasarkan slug.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| slug | String | URL-friendly identifier perusahaan |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully get company profile",
  "result": {
    "id": 1,
    "name": "TechCorp Indonesia",
    "slug": "techcorp-indonesia",
    "description": "Perusahaan teknologi terdepan",
    "logo": "https://minio.example.com/logo.png",
    "website": "https://techcorp.example.com",
    "email": "contact@techcorp.example.com",
    "phone": "021-1234567",
    "address": "Jalan Sudirman No. 123, Jakarta",
    "rating": 4.5,
    "reviewCount": 120,
    "category": "Technology"
  }
}
```

### Error Response (404 Not Found)

```json
{
  "success": false,
  "message": "Company not found"
}
```

---

## Response Format

Semua endpoint mengembalikan `WebResponse` dengan struktur:

```json
{
  "success": boolean,
  "message": string,
  "result": object | array,
  "meta": object (optional)
}
```

## Authentication

- **Get Companies:** Public
- **Submit Request:** Requires authentication
- **Get Requests:** Admin only
- **Review Request:** Admin only
- **Get Request Detail:** Requires authentication
- **Top Companies:** Public
- **Company Profile:** Public
