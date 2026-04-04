# Dokumentasi API — Perusahaan

Base path: `/company`

---

## GET /company

**Autentikasi**: Publik

Mengambil daftar perusahaan dengan cursor-based pagination. Jika parameter `search` diberikan, endpoint ini melakukan pencarian teks pada nama perusahaan.

### Query Parameters

| Parameter | Tipe    | Wajib | Default | Keterangan                                               |
|-----------|---------|-------|---------|----------------------------------------------------------|
| cursor    | long    | Tidak | —       | ID terakhir dari halaman sebelumnya (untuk pagination)   |
| limit     | integer | Tidak | 15      | Jumlah data per halaman                                  |
| search    | string  | Tidak | —       | Kata kunci pencarian nama perusahaan (mengabaikan cursor/limit jika diisi) |

### Contoh Response — Mode Pagination

```json
{
  "success": true,
  "message": "Successfully get companies",
  "meta": {
    "nextCursor": 20,
    "hasMore": true
  },
  "result": [
    {
      "id": 1,
      "companyName": "PT Contoh Teknologi",
      "slug": "pt-contoh-teknologi",
      "isPartner": true,
      "averageRating": 4.2
    }
  ]
}
```

### Contoh Response — Mode Pencarian

```json
{
  "success": true,
  "message": "Successfully search companies",
  "result": [
    {
      "id": 1,
      "companyName": "PT Contoh Teknologi",
      "slug": "pt-contoh-teknologi"
    }
  ]
}
```

---

## POST /company/requests

**Autentikasi**: Harus login

Mengajukan permintaan penambahan perusahaan baru.

### Request Body

| Field               | Tipe    | Wajib | Validasi                  |
|---------------------|---------|-------|---------------------------|
| companyName         | string  | Ya    | Panjang 3–65 karakter     |
| companyAbbreviation | string  | Tidak | Maksimal 15 karakter      |
| website             | string  | Tidak | Maksimal 35 karakter      |
| isPartner           | boolean | Tidak | —                         |
| subcategoryId       | long    | Tidak | —                         |

### Contoh Request Body

```json
{
  "companyName": "PT Inovasi Digital",
  "companyAbbreviation": "PTID",
  "website": "https://inovasidigital.id",
  "isPartner": false,
  "subcategoryId": 7
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully submit company request",
  "result": {
    "id": 12,
    "companyName": "PT Inovasi Digital",
    "status": "PENDING",
    "submittedAt": "2026-04-02T09:30:00"
  }
}
```

---

## GET /company/requests

**Autentikasi**: Hanya ADMIN

Mengambil daftar pengajuan perusahaan dengan cursor-based pagination.

### Query Parameters

| Parameter | Tipe   | Wajib | Default | Keterangan                                             |
|-----------|--------|-------|---------|--------------------------------------------------------|
| status    | string | Tidak | —       | Filter berdasarkan status: `PENDING`, `APPROVED`, `REJECTED` |
| cursor    | long   | Tidak | —       | ID terakhir dari halaman sebelumnya                    |
| limit     | integer| Tidak | 15      | Jumlah data per halaman                                |

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully get company requests",
  "meta": {
    "nextCursor": 30,
    "hasMore": true
  },
  "result": [
    {
      "id": 12,
      "companyName": "PT Inovasi Digital",
      "status": "PENDING",
      "submittedBy": {
        "id": 1,
        "firstName": "Budi"
      },
      "submittedAt": "2026-04-02T09:30:00"
    }
  ]
}
```

---

## PATCH /company/requests/{requestId}/review

**Autentikasi**: Hanya ADMIN

Meninjau (menyetujui atau menolak) pengajuan perusahaan.

### Path Parameters

| Parameter | Tipe | Keterangan                                  |
|-----------|------|---------------------------------------------|
| requestId | long | ID pengajuan perusahaan yang akan ditinjau  |

### Request Body

| Field      | Tipe   | Wajib | Validasi                                                 |
|------------|--------|-------|----------------------------------------------------------|
| status     | string | Ya    | Nilai yang valid ditentukan oleh `@ValidReviewStatus`    |
| reviewNote | string | Tidak | Maksimal 255 karakter                                    |

### Contoh Request Body

```json
{
  "status": "APPROVED",
  "reviewNote": "Data perusahaan sudah lengkap dan terverifikasi."
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully review company request",
  "result": {
    "id": 12,
    "companyName": "PT Inovasi Digital",
    "status": "APPROVED",
    "reviewNote": "Data perusahaan sudah lengkap dan terverifikasi.",
    "reviewedAt": "2026-04-02T10:15:00"
  }
}
```

---

## GET /company/request/{requestId}

**Autentikasi**: Harus login

Mengambil detail pengajuan perusahaan berdasarkan ID.

### Path Parameters

| Parameter | Tipe | Keterangan              |
|-----------|------|-------------------------|
| requestId | long | ID pengajuan perusahaan |

### Contoh Response

```json
{
  "success": true,
  "message": "Company request detail",
  "result": {
    "id": 12,
    "companyName": "PT Inovasi Digital",
    "companyAbbreviation": "PTID",
    "website": "https://inovasidigital.id",
    "isPartner": false,
    "subcategoryId": 7,
    "status": "PENDING",
    "reviewNote": null,
    "submittedAt": "2026-04-02T09:30:00",
    "reviewedAt": null
  }
}
```

---

## GET /company/top-ratings

**Autentikasi**: Publik

Mengambil daftar 10 perusahaan dengan rata-rata rating tertinggi.

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully get top 10 companies",
  "result": [
    {
      "id": 5,
      "companyName": "PT Teknologi Maju",
      "slug": "pt-teknologi-maju",
      "averageRating": 4.8,
      "totalReviews": 32
    },
    {
      "id": 3,
      "companyName": "CV Solusi Kreatif",
      "slug": "cv-solusi-kreatif",
      "averageRating": 4.6,
      "totalReviews": 18
    }
  ]
}
```

---

## GET /company/{slug}

**Autentikasi**: Publik

Mengambil profil lengkap perusahaan berdasarkan slug.

### Path Parameters

| Parameter | Tipe   | Keterangan        |
|-----------|--------|-------------------|
| slug      | string | Slug perusahaan   |

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully get company profile",
  "result": {
    "id": 5,
    "companyName": "PT Teknologi Maju",
    "companyAbbreviation": "PTM",
    "slug": "pt-teknologi-maju",
    "website": "https://teknologimaju.id",
    "isPartner": true,
    "subcategory": {
      "id": 7,
      "subCategoryName": "Software Development"
    },
    "averageRating": 4.8,
    "totalReviews": 32
  }
}
```
