# Lookup Controller Documentation

Base URL: `/lookup`

---

## 1. Get All Lookups

**Endpoint:** `GET /lookup`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Mendapatkan daftar lengkap semua lookup data yang tersedia di sistem.

### Request

```bash
curl -X GET "http://localhost:8080/lookup"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get All Lookups",
  "result": [
    {
      "lookupId": 1,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "CVS",
      "lookupDescription": "CV Screening",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 2,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "CS",
      "lookupDescription": "Case Study",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 3,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "HRINT",
      "lookupDescription": "HR Interview",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 4,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "UINT",
      "lookupDescription": "User Interview",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 5,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "TT",
      "lookupDescription": "Technical Test",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 6,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "PS",
      "lookupDescription": "Presentation",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    }
  ]
}
```

### Response Fields

| Field | Type | Deskripsi |
|-------|------|-----------|
| lookupId | Long | ID lookup unik |
| lookupType | String | Tipe/kategori lookup (contoh: `INTERNSHIP_REVIEW`) |
| lookupCode | String | Kode grouping untuk lookup sejenis (contoh: `RECRUITMENT_STEPS`) |
| lookupValue | String | Nilai lookup (contoh: `CVS`) |
| lookupDescription | String | Deskripsi lookup untuk display (contoh: `CV Screening`) |
| createdAt | DateTime | Waktu lookup dibuat |
| createdBy | Long | ID user yang membuat lookup |
| updatedAt | DateTime | Waktu lookup diupdate terakhir |
| updatedBy | Long | ID user yang mengupdate lookup |

---

## 2. Get Lookups By Type

**Endpoint:** `GET /lookup/{type}`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Mendapatkan daftar lookup berdasarkan tipe/kategori tertentu.

### Request Parameters

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| type | String | Ya | Tipe lookup yang ingin difilter (contoh: `INTERNSHIP_REVIEW`) |

### Example Request

```bash
curl -X GET "http://localhost:8080/lookup/INTERNSHIP_REVIEW"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Lookups by Type",
  "result": [
    {
      "lookupId": 1,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "CVS",
      "lookupDescription": "CV Screening",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 2,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "CS",
      "lookupDescription": "Case Study",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 3,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "HRINT",
      "lookupDescription": "HR Interview",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 4,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "UINT",
      "lookupDescription": "User Interview",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 5,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "TT",
      "lookupDescription": "Technical Test",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    },
    {
      "lookupId": 6,
      "lookupType": "INTERNSHIP_REVIEW",
      "lookupCode": "RECRUITMENT_STEPS",
      "lookupValue": "PS",
      "lookupDescription": "Presentation",
      "createdAt": "2024-01-15T10:30:00+07:00",
      "createdBy": null,
      "updatedAt": null,
      "updatedBy": null
    }
  ]
}
```

### Empty Result Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Lookups by Type",
  "result": []
}
```

### Response Fields

Sama seperti endpoint 1 di atas.
