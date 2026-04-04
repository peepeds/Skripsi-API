# Dokumentasi API — Pengguna

Base path: `/user`

---

## GET /user

**Autentikasi**: Hanya ADMIN

Mengambil daftar semua pengguna dengan privilege `USER`.

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully get all user",
  "result": [
    {
      "id": 1,
      "firstName": "Budi",
      "lastName": "Santoso",
      "email": "budi@mahasiswa.ac.id",
      "phoneNumber": "081234567890",
      "regionId": 1,
      "majorId": 3,
      "active": true
    }
  ]
}
```

---

## GET /user/me

**Autentikasi**: Harus login

Mengambil profil pengguna yang sedang login.

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully get profile",
  "result": {
    "id": 1,
    "firstName": "Budi",
    "lastName": "Santoso",
    "email": "budi@mahasiswa.ac.id",
    "phoneNumber": "081234567890",
    "registerId": "2021001",
    "region": {
      "id": 1,
      "regionName": "Jawa Barat"
    },
    "major": {
      "id": 3,
      "majorName": "Teknik Informatika"
    }
  }
}
```

---

## POST /user/check-email

**Autentikasi**: Publik

Memeriksa apakah suatu email sudah terdaftar atau belum.

### Request Body

| Field | Tipe   | Wajib | Validasi           |
|-------|--------|-------|--------------------|
| email | string | Ya    | Tidak boleh kosong |

### Contoh Request Body

```json
{
  "email": "budi@mahasiswa.ac.id"
}
```

### Contoh Response — Email Tersedia (200)

```json
{
  "success": true,
  "message": "Email is available"
}
```

### Contoh Response — Email Sudah Digunakan (409)

```json
{
  "success": false,
  "message": "Email already used"
}
```

### Contoh Response — Email Tidak Disertakan (400)

```json
{
  "success": false,
  "message": "Email is required"
}
```

---

## POST /user/certificate

**Autentikasi**: Harus login

Mengajukan permintaan unggah sertifikat baru.

### Request Body

| Field           | Tipe    | Wajib | Validasi |
|-----------------|---------|-------|----------|
| issuer          | string  | Tidak | —        |
| certificateUrl  | string  | Tidak | —        |
| certificateName | string  | Tidak | —        |
| fileSize        | long    | Tidak | —        |

### Contoh Request Body

```json
{
  "issuer": "Dicoding Indonesia",
  "certificateName": "Belajar Machine Learning",
  "certificateUrl": "https://storage.example.com/cert/abc123.pdf",
  "fileSize": 204800
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Certificate request submitted",
  "result": {
    "id": 5,
    "issuer": "Dicoding Indonesia",
    "certificateName": "Belajar Machine Learning",
    "certificateUrl": "https://storage.example.com/cert/abc123.pdf",
    "fileSize": 204800,
    "status": "PENDING",
    "submittedAt": "2026-04-02T10:00:00"
  }
}
```

---

## PATCH /user/certificate/requests/{requestId}/review

**Autentikasi**: Hanya ADMIN

Meninjau (menyetujui atau menolak) pengajuan sertifikat.

### Path Parameters

| Parameter | Tipe | Keterangan                                     |
|-----------|------|------------------------------------------------|
| requestId | long | ID pengajuan sertifikat yang akan ditinjau     |

### Request Body

| Field      | Tipe   | Wajib | Validasi                          |
|------------|--------|-------|-----------------------------------|
| status     | string | Tidak | Nilai yang valid: `APPROVED`, `REJECTED` |
| reviewNote | string | Tidak | —                                 |

### Contoh Request Body

```json
{
  "status": "APPROVED",
  "reviewNote": "Dokumen valid dan terverifikasi."
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Certificate request reviewed",
  "result": {
    "id": 5,
    "issuer": "Dicoding Indonesia",
    "certificateName": "Belajar Machine Learning",
    "status": "APPROVED",
    "reviewNote": "Dokumen valid dan terverifikasi.",
    "reviewedAt": "2026-04-02T11:00:00"
  }
}
```

---

## GET /user/certificate/request/{requestId}

**Autentikasi**: Harus login

Mengambil detail pengajuan sertifikat berdasarkan ID.

### Path Parameters

| Parameter | Tipe | Keterangan              |
|-----------|------|-------------------------|
| requestId | long | ID pengajuan sertifikat |

### Contoh Response

```json
{
  "success": true,
  "message": "Certificate request detail",
  "result": {
    "id": 5,
    "issuer": "Dicoding Indonesia",
    "certificateName": "Belajar Machine Learning",
    "certificateUrl": "https://storage.example.com/cert/abc123.pdf",
    "fileSize": 204800,
    "status": "PENDING",
    "reviewNote": null,
    "submittedAt": "2026-04-02T10:00:00",
    "reviewedAt": null
  }
}
```
