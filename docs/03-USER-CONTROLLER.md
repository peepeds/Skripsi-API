# User Controller Documentation

Base URL: `/user`

---

## 1. Get All Users By Privilege

**Endpoint:** `GET /user`

**Access:** Autentikasi diperlukan (User login)

**Deskripsi:** Mendapatkan daftar pengguna berdasarkan privilege level user yang login. Admin bisa melihat semua user, kepala department hanya melihat user di departementnya, dll.

### Request

Tidak ada parameter, hanya kirim dengan authentication token.

```bash
curl -X GET "http://localhost:8080/user" \
  -H "Authorization: Bearer <access_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get All User",
  "result": [
    {
      "userId": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "regionName": "Kemanggisan",
      "majorName": "Computer Science",
      "deptName": "SoCS"
    },
    {
      "userId": 2,
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@example.com",
      "regionName": "Alam Sutera",
      "majorName": "Information Systems",
      "deptName": "SoIS"
    }
  ]
}
```

### Error Response

- **401 Unauthorized**: Tidak terautentikasi

---

## 2. Get My Profile

**Endpoint:** `GET /user/me`

**Access:** Autentikasi diperlukan (User login)

**Deskripsi:** Mendapatkan profil lengkap user yang sedang login.

### Request

```bash
curl -X GET "http://localhost:8080/user/me" \
  -H "Authorization: Bearer <access_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully get profile",
  "result": {
    "userId": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "studentId": "BIN2024001",
    "lectureId": "LEC01",
    "phoneNumber": "081234567890",
    "regionName": "Kemanggisan",
    "majorName": "Computer Science",
    "deptName": "SoCS",
    "certificates": [
      {
        "userCertificateId": 1,
        "issuer": "PT. Example Company",
        "certificatesUrl": "https://minio.example.com/certificates/cert1.pdf",
        "createdAt": "2026-03-01T10:00:00Z",
        "updatedAt": "2026-03-01T10:00:00Z"
      }
    ],
    "role": "USER"
  }
}
```

> **Note:** Response now includes `deptName` (department info) for the logged-in user.

### Error Response

- **401 Unauthorized**: Tidak terautentikasi

---

## 3. Check Email Availability

**Endpoint:** `POST /user/check-email`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Mengecek apakah email sudah terdaftar di sistem atau masih tersedia untuk registrasi.

### Request Body

```json
{
  "email": "newuser@example.com"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| email | String | Ya | Email yang ingin dicek ketersediaannya |

### Example Request

```bash
curl -X POST "http://localhost:8080/user/check-email" \
  -H "Content-Type: application/json" \
  -d '{"email": "newuser@example.com"}'
```

### Success Response - Email Available (200 OK)

```json
{
  "success": true,
  "message": "Email is available"
}
```

### Success Response - Email Already Used (409 Conflict)

```json
{
  "success": false,
  "message": "Email already used!"
}
```

### Error Response

- **400 Bad Request**: Email tidak dikirim atau kosong

---

## 4. Upload Certificate

**Endpoint:** `POST /user/certificate`

**Access:** Autentikasi diperlukan (User login)

**Deskripsi:** Mengupload sertifikat magang. Sertifikat akan masuk ke dalam request documents dan menunggu approval dari admin.

### Request Body

```json
{
  "issuer": "PT. Example Company",
  "certificatesUrl": "https://minio.example.com/certificates/cert1.pdf",
  "certificateName": "Internship Certificate",
  "fileSize": 2048000
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| issuer | String | Ya | Penerbit sertifikat |
| certificatesUrl | String | Ya | URL file sertifikat yang sudah diupload ke MinIO |
| certificateName | String | Ya | Nama sertifikat |
| fileSize | Long | Ya | Ukuran file dalam bytes |

### Example Request

```bash
curl -X POST "http://localhost:8080/user/certificate" \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "issuer": "PT. Example Company",
    "certificatesUrl": "https://minio.example.com/certificates/cert1.pdf",
    "certificateName": "Internship Certificate",
    "fileSize": 2048000
  }'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Certificate request submitted successfully",
  "result": {
    "userCertificateId": null,
    "issuer": "PT. Example Company",
    "certificatesUrl": "https://minio.example.com/certificates/cert1.pdf",
    "createdAt": "2026-03-01T10:00:00Z",
    "updatedAt": "2026-03-01T10:00:00Z"
  }
}
```

### Error Response

- **401 Unauthorized**: Tidak terautentikasi

---

## 5. Review Certificate Request (Admin Only)

**Endpoint:** `PATCH /user/certificate/requests/{requestId}/review`

**Access:** Admin only (Role ADMIN)

**Deskripsi:** Admin dapat menyetujui atau menolak request sertifikat. Jika disetujui, sertifikat akan dipindahkan ke tabel user_certificates.

### Path Parameters

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| requestId | Long | Ya | ID dari request document sertifikat |

### Request Body

```json
{
  "status": "APPROVED",
  "reviewNote": "Sertifikat valid"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| status | String | Ya | Status review: "APPROVED" atau "REJECTED" |
| reviewNote | String | Tidak | Catatan review dari admin |

### Example Request

```bash
curl -X PATCH "http://localhost:8080/user/certificate/requests/123/review" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED",
    "reviewNote": "Sertifikat valid"
  }'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Certificate request reviewed successfully",
  "result": {
    "userCertificateId": null,
    "issuer": "Internship Certificate",
    "certificatesUrl": "https://minio.example.com/certificates/cert1.pdf",
    "createdAt": "2026-03-01T10:00:00Z",
    "updatedAt": "2026-03-01T10:00:00Z"
  }
}
```

### Error Response

- **401 Unauthorized**: Tidak terautentikasi
- **403 Forbidden**: Tidak memiliki role ADMIN
- **400 Bad Request**: Request sudah di-review sebelumnya

---

## 6. Get Certificate Request Detail

**Endpoint:** `GET /user/certificate/request/{requestId}`

**Access:** Autentikasi diperlukan (User login)

**Deskripsi:** Mendapatkan detail request sertifikat. Hanya pemilik request yang dapat melihat detailnya.

### Path Parameters

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| requestId | Long | Ya | ID dari request document sertifikat |

### Example Request

```bash
curl -X GET "http://localhost:8080/user/certificate/request/123" \
  -H "Authorization: Bearer <access_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Certificate request detail",
  "result": {
    "requestDetails": {
      "requestId": 123,
      "certificateName": "Internship Certificate",
      "certificatesUrl": "https://minio.example.com/certificates/cert1.pdf",
      "fileSize": 2048000,
      "submittedAt": "2026-03-01T10:00:00Z",
      "submittedBy": "John"
    },
    "reviewInformation": {
      "status": "PENDING",
      "reviewedAt": null,
      "reviewNote": null,
      "reviewedBy": null
    }
  }
}
```

### Error Response

- **401 Unauthorized**: Tidak terautentikasi
- **403 Forbidden**: Tidak memiliki akses ke request ini

---

## Important Notes

- **Privilege Level**: Daftar user yang ditampilkan di GET /user disesuaikan dengan privilege level user yang login
- **Profile Detail**: GET /user/me menampilkan detail profil lengkap termasuk role dan informasi pendidikan
- **Email Check**: Endpoint check-email sangat berguna sebelum form registrasi untuk memberikan feedback real-time
- **Security**: Email check bersifat publik untuk user experience yang lebih baik, tidak membocorkan informasi sensitif lainnya
- **Certificate Upload**: Sertifikat harus diupload ke MinIO terlebih dahulu sebelum submit request
- **Certificate Approval**: Hanya admin yang dapat menyetujui request sertifikat
- **Certificate Access**: User hanya dapat melihat detail request sertifikat miliknya sendiri
