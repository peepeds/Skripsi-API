# Dokumentasi API — Lookup

Base path: `/lookup`

---

## GET /lookup

**Autentikasi**: Publik

Mengambil semua data lookup dari seluruh tipe yang tersedia.

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Get All Lookups",
  "result": [
    {
      "id": 1,
      "type": "INTERNSHIP_TYPE",
      "value": "Magang",
      "label": "Magang"
    },
    {
      "id": 2,
      "type": "INTERNSHIP_TYPE",
      "value": "MSIB",
      "label": "Magang Studi Independen Bersertifikat"
    },
    {
      "id": 3,
      "type": "WORK_SCHEME",
      "value": "WFH",
      "label": "Work From Home"
    },
    {
      "id": 4,
      "type": "WORK_SCHEME",
      "value": "WFO",
      "label": "Work From Office"
    }
  ]
}
```

---

## GET /lookup/{type}

**Autentikasi**: Publik

Mengambil data lookup berdasarkan tipe tertentu.

### Path Parameters

| Parameter | Tipe   | Keterangan                                                            |
|-----------|--------|-----------------------------------------------------------------------|
| type      | string | Tipe lookup, contoh: `INTERNSHIP_TYPE`, `WORK_SCHEME`, `REVIEW_STATUS` |

### Contoh Request

```
GET /lookup/INTERNSHIP_TYPE
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Get Lookups by Type",
  "result": [
    {
      "id": 1,
      "type": "INTERNSHIP_TYPE",
      "value": "Magang",
      "label": "Magang"
    },
    {
      "id": 2,
      "type": "INTERNSHIP_TYPE",
      "value": "MSIB",
      "label": "Magang Studi Independen Bersertifikat"
    }
  ]
}
```
