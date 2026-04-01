# Lookup Controller Documentation

Base URL: `/lookup`

---

## 1. Get All Lookups

**Endpoint:** `GET /lookup`

**Access:** Public

**Deskripsi:** Mengambil semua data lookup yang tersedia dalam sistem.

### Request

- Method: GET
- No parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get All Lookups",
  "result": [
    {
      "id": 1,
      "type": "EMPLOYMENT_TYPE",
      "key": "FULL_TIME",
      "value": "Full Time",
      "label": "Full Time Employment"
    },
    {
      "id": 2,
      "type": "EMPLOYMENT_TYPE",
      "key": "PART_TIME",
      "value": "Part Time",
      "label": "Part Time Employment"
    },
    {
      "id": 3,
      "type": "JOB_LEVEL",
      "key": "JUNIOR",
      "value": "Junior",
      "label": "Junior Level"
    }
  ]
}
```

---

## 2. Get Lookups by Type

**Endpoint:** `GET /lookup/{type}`

**Access:** Public

**Deskripsi:** Mengambil data lookup berdasarkan tipe tertentu.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| type | String | Tipe lookup (EMPLOYMENT_TYPE, JOB_LEVEL, CERTIFICATE_TYPE, dll) |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Lookups by Type",
  "result": [
    {
      "id": 1,
      "type": "EMPLOYMENT_TYPE",
      "key": "FULL_TIME",
      "value": "Full Time",
      "label": "Full Time Employment"
    },
    {
      "id": 2,
      "type": "EMPLOYMENT_TYPE",
      "key": "PART_TIME",
      "value": "Part Time",
      "label": "Part Time Employment"
    },
    {
      "id": 3,
      "type": "EMPLOYMENT_TYPE",
      "key": "CONTRACT",
      "value": "Contract",
      "label": "Contract Employment"
    }
  ]
}
```

### Success Response - Empty Result (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Lookups by Type",
  "result": []
}
```

---

## Response Format

Semua endpoint mengembalikan `WebResponse` dengan struktur:

```json
{
  "success": boolean,
  "message": string,
  "result": array
}
```

## Lookup Types

| Type | Deskripsi | Contoh Nilai |
|------|-----------|--------------|
| EMPLOYMENT_TYPE | Tipe pekerjaan | FULL_TIME, PART_TIME, CONTRACT, FREELANCE |
| JOB_LEVEL | Level pekerjaan | JUNIOR, SENIOR, LEAD, MANAGER |
| CERTIFICATE_TYPE | Tipe sertifikat | LANGUAGE, TECHNICAL, PROFESSIONAL |
| COMPANY_SIZE | Ukuran perusahaan | STARTUP, SMALL, MEDIUM, LARGE, ENTERPRISE |
| INDUSTRY | Industri | IT, FINANCE, HEALTHCARE, EDU, RETAIL |

## Authentication

- **Get All Lookups:** Public
- **Get Lookups by Type:** Public

## Notes

- Lookup data adalah data yang relatif statis dan tidak sering berubah
- Ideal untuk cache pada client-side
- Digunakan untuk populate dropdown/select pada forms
