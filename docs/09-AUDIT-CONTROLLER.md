# Audit Controller Documentation

Base URL: `/audit`

---

## Overview

Audit log mencatat semua aktivitas penting (submit, approve, reject, dll) untuk setiap entitas. Endpoint ini bersifat **generic** — satu endpoint untuk semua tipe entitas, diidentifikasi via query param `entity` + `id`.

---

## 1. Get Audit Logs

**Endpoint:** `GET /audit`

**Access:** Autentikasi diperlukan (User login)

**Deskripsi:** Mendapatkan riwayat aktivitas dari sebuah entitas, diurutkan dari yang paling lama ke yang paling baru.

### Request Parameters

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| entity | String | Ya | Tipe entitas: `COMPANY_REQUEST`, dll |
| id | Long | Ya | ID dari entitas yang dimaksud |

### Example Request

```bash
curl -X GET "http://localhost:8080/audit?entity=COMPANY_REQUEST&id=4" \
  -H "Authorization: Bearer <access_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Audit log fetched",
  "result": [
    {
      "action": "SUBMITTED",
      "actor": "Edbert Santoso",
      "timestamp": "2026-02-27T17:24:01Z"
    },
    {
      "action": "APPROVED",
      "actor": "Admin User",
      "timestamp": "2026-02-27T17:28:20Z"
    }
  ]
}
```

### Response Fields

| Field | Type | Deskripsi |
|-------|------|-----------|
| action | String | Aksi yang dilakukan: `SUBMITTED`, `APPROVED`, `REJECTED`, dll |
| actor | String | Nama user yang melakukan aksi |
| timestamp | DateTime | Waktu aksi dilakukan |

### Error Response

- **400 Bad Request**: `entity` atau `id` tidak valid / tidak dikenali
- **401 Unauthorized**: Tidak terautentikasi

---

## Supported Entities

| entity | Aksi yang dicatat | Detail Endpoint |
|--------|-------------------|-----------------|
| `COMPANY_REQUEST` | `SUBMITTED`, `APPROVED`, `REJECTED` | `GET /company/request/{id}` |

> Entitas baru cukup ditambahkan di `AuditService.VALID_ENTITIES` dan ditulis via `auditService.record(...)`.

---

## Important Notes

- **Generic**: Satu endpoint untuk semua tipe entitas — tidak ada `/company/audit`, `/department/audit`, dll
- **Chronological**: Hasil selalu diurutkan dari aksi pertama (terlama) ke terbaru
- **Actor**: Nama aktor di-resolve dari `userId` ke `firstName + lastName`

