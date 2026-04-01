# User Controller Documentation

Base URL: `/user`

---

## 1. Get All Users

**Endpoint:** `GET /user`

**Access:** Admin only (`hasRole('ADMIN')`)

**Deskripsi:** Mengambil daftar semua pengguna sesuai dengan privilege yang dimiliki.

### Request

- Method: GET
- No query parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get All User",
  "result": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "role": "USER"
    }
  ]
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

## 2. Get Current User Profile

**Endpoint:** `GET /user/me`

**Access:** Authenticated users (`isAuthenticated()`)

**Deskripsi:** Mengambil profil pengguna yang sedang login.

### Request

- Method: GET
- No parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully get profile",
  "result": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "081234567890",
    "region": {
      "id": 1,
      "name": "Jakarta"
    },
    "major": {
      "id": 5,
      "name": "Computer Science"
    }
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

## 3. Check Email Availability

**Endpoint:** `POST /user/check-email`

**Access:** Public

**Deskripsi:** Memeriksa apakah email sudah digunakan atau masih tersedia.

### Request Body

```json
{
  "email": "newuser@example.com"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| email | String | Ya | Email yang akan diperiksa |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Email is available"
}
```

### Error Response - Email Already Used (409 Conflict)

```json
{
  "success": false,
  "message": "Email already used!"
}
```

### Error Response - Empty Email (400 Bad Request)

```json
{
  "success": false,
  "message": "Email is required"
}
```

---

## 4. Upload Certificate

**Endpoint:** `POST /user/certificate`

**Access:** Authenticated users (`isAuthenticated()`)

**Deskripsi:** Mengajukan sertifikat untuk diverifikasi oleh admin.

### Request Body

```json
{
  "certificateType": "LANGUAGE",
  "issuingOrganization": "Cambridge University",
  "issueDate": "2024-01-15",
  "expiryDate": "2029-01-15",
  "fileUrl": "https://minio.example.com/cert.pdf"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| certificateType | String | Ya | Tipe sertifikat |
| issuingOrganization | String | Ya | Organisasi penerbit |
| issueDate | Date | Ya | Tanggal terbit |
| expiryDate | Date | Tidak | Tanggal kadaluarsa |
| fileUrl | String | Ya | URL file sertifikat |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Certificate request submitted successfully",
  "result": {
    "id": 1,
    "status": "PENDING",
    "certificateType": "LANGUAGE"
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

## 5. Review Certificate Request

**Endpoint:** `PATCH /user/certificate/requests/{requestId}/review`

**Access:** Admin only (`hasRole('ADMIN')`)

**Deskripsi:** Admin mereview sertifikat yang diajukan pengguna.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| requestId | Long | ID dari certificate request |

### Request Body

```json
{
  "status": "APPROVED",
  "notes": "Sertifikat valid dan sesuai"
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
  "message": "Certificate request reviewed successfully",
  "result": {
    "id": 1,
    "status": "APPROVED",
    "notes": "Sertifikat valid dan sesuai"
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

## 6. Get Certificate Request Detail

**Endpoint:** `GET /user/certificate/request/{requestId}`

**Access:** Authenticated users (`isAuthenticated()`)

**Deskripsi:** Mengambil detail dari sertifikat request tertentu.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| requestId | Long | ID dari certificate request |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Certificate request detail",
  "result": {
    "id": 1,
    "userId": 5,
    "status": "PENDING",
    "certificateType": "LANGUAGE",
    "issuingOrganization": "Cambridge University",
    "issueDate": "2024-01-15",
    "expiryDate": "2029-01-15",
    "fileUrl": "https://minio.example.com/cert.pdf",
    "reviewNotes": null,
    "createdAt": "2024-02-01T10:30:00Z"
  }
}
```

### Error Response (404 Not Found)

```json
{
  "success": false,
  "message": "Certificate request not found"
}
```

---

## Response Format

Semua endpoint mengembalikan `WebResponse` dengan struktur:

```json
{
  "success": boolean,
  "message": string,
  "result": object (optional)
}
```

## Authentication

- **Get All Users:** Admin only
- **Get Profile:** Requires authentication
- **Check Email:** Public
- **Upload Certificate:** Requires authentication
- **Review Certificate:** Admin only
- **Get Certificate Detail:** Requires authentication
