# Dokumentasi API — Review

Base path: `/review`

---

## GET /review/jobOptions

**Autentikasi**: Publik

Mencari daftar pilihan pekerjaan (job options) untuk digunakan saat mengisi formulir review. Mendukung pencarian teks opsional.

### Query Parameters

| Parameter | Tipe   | Wajib | Keterangan                          |
|-----------|--------|-------|-------------------------------------|
| query     | string | Tidak | Kata kunci pencarian nama pekerjaan |

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully search job options",
  "result": [
    {
      "id": 10,
      "jobTitle": "Backend Developer"
    },
    {
      "id": 11,
      "jobTitle": "Fullstack Developer"
    }
  ]
}
```

---

## GET /review/recent

**Autentikasi**: Publik

Mengambil daftar review terbaru dari seluruh perusahaan.

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully retrieved recent reviews",
  "result": [
    {
      "id": 45,
      "companyName": "PT Teknologi Maju",
      "companySlug": "pt-teknologi-maju",
      "jobTitle": "Backend Developer",
      "internshipType": "Remote",
      "workScheme": "WFH",
      "year": 2025,
      "averageRating": 4.2,
      "submittedAt": "2026-03-28T14:22:00"
    }
  ]
}
```

---

## POST /review/{slug}

**Autentikasi**: Harus login

Mengajukan review untuk perusahaan yang ditentukan oleh slug.

### Path Parameters

| Parameter | Tipe   | Keterangan      |
|-----------|--------|-----------------|
| slug      | string | Slug perusahaan |

### Request Body

| Field                   | Tipe           | Wajib | Validasi                               |
|-------------------------|----------------|-------|----------------------------------------|
| internshipType          | string         | Ya    | Tidak boleh kosong, maks. 10 karakter  |
| workScheme              | string         | Ya    | Tidak boleh kosong, maks. 10 karakter  |
| duration                | integer        | Ya    | Minimal 1                              |
| year                    | integer        | Ya    | Minimal 2000                           |
| jobTitle                | string         | Ya    | Tidak boleh kosong, maks. 75 karakter  |
| SubCategoryIds          | array of long  | Tidak | Maksimal 5 item                        |
| ratings                 | object         | Ya    | Lihat detail objek `ratings` di bawah |
| recruitmentSteps        | array of long  | Tidak | Maksimal 10 item, kode langkah seleksi |
| interviewDifficulty     | integer        | Ya    | Min 0, Maks 5                          |
| testimony               | string         | Ya    | Tidak boleh kosong, maks. 500 karakter |
| pros                    | string         | Ya    | Tidak boleh kosong, maks. 500 karakter |
| cons                    | string         | Ya    | Tidak boleh kosong, maks. 500 karakter |
| admissionTrack          | string         | Tidak | Maks. 10 karakter                      |
| recruitmentDurationCode | string         | Tidak | Maks. 10 karakter                      |
| exampleQuestions        | string         | Tidak | Maks. 500 karakter                     |
| selectionProcess        | string         | Tidak | Maks. 500 karakter                     |
| tipsTricks              | string         | Tidak | Maks. 500 karakter                     |

**Objek `ratings`**

| Field           | Tipe    | Wajib | Validasi       |
|-----------------|---------|-------|----------------|
| workCulture     | integer | Ya    | Min 0, Maks 5  |
| learningOpp     | integer | Ya    | Min 0, Maks 5  |
| mentorship      | integer | Ya    | Min 0, Maks 5  |
| benefit         | integer | Ya    | Min 0, Maks 5  |
| workLifeBalance | integer | Ya    | Min 0, Maks 5  |

### Contoh Request Body

```json
{
  "internshipType": "P",
  "workScheme": "WFO",
  "duration": 4,
  "year": 2025,
  "jobTitle": "Software Engineer",
  "SubCategoryIds": [18],
  "ratings": {
    "workCulture": 4,
    "learningOpp": 5,
    "mentorship": 4,
    "benefit": 5,
    "workLifeBalance": 5
  },
  "recruitmentSteps": [1, 3],
  "interviewDifficulty": 5,
  "testimony": "Magang disini sangat seru",
  "pros": "Mentor baik",
  "cons": "Sering lembur",
  "admissionTrack": "ONLINE",
  "recruitmentDurationCode": "2W",
  "exampleQuestions": "Contoh:\n- Ceritakan pengalaman organisasi...\n- Apa motivasi kamu...\n- Jelaskan proyek...",
  "selectionProcess": "CV screening, interview HR, technical test",
  "tipsTricks": "Baca profil perusahaan sebelum interview"
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Review submitted successfully",
  "result": {
    "id": 101,
    "companySlug": "pt-teknologi-maju",
    "jobTitle": "Backend Developer",
    "status": "PUBLISHED",
    "submittedAt": "2026-04-02T10:45:00"
  }
}
```

---

## GET /review/{slug}/summary

**Autentikasi**: Publik

Mengambil ringkasan rating keseluruhan untuk suatu perusahaan.

### Path Parameters

| Parameter | Tipe   | Keterangan      |
|-----------|--------|-----------------|
| slug      | string | Slug perusahaan |

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully retrieved company review summary",
  "result": {
    "informationDetails": {
      "type": "P",
      "workScheme": ["WFO", "WFH"],
      "duration": "3 - 6 months",
      "subCategories": ["Application Development", "Mobile Development"]
    },
    "ratings": {
      "workCulture": 4.5,
      "learningOpp": 4.6,
      "mentorship": 4.1,
      "benefit": 3.9,
      "workLifeBalance": 4.4
    },
    "recruitmentProcesses": {
      "rating": 3.5,
      "steps": ["Screening CV", "Wawancara User"]
    }
  }
}
```

> `steps` berisi deskripsi dari lookup `RECRUITMENT_STEPS`, bukan kode numerik.

---

## GET /review/{slug}

**Autentikasi**: Publik

Mengambil daftar review untuk suatu perusahaan dengan cursor-based pagination.

### Path Parameters

| Parameter | Tipe   | Keterangan      |
|-----------|--------|-----------------|
| slug      | string | Slug perusahaan |

### Query Parameters

| Parameter | Tipe    | Wajib | Default  | Keterangan                                      |
|-----------|---------|-------|----------|-------------------------------------------------|
| order     | string  | Tidak | popular  | Urutan tampilan: `popular` atau `recent`        |
| cursor    | long    | Tidak | —        | ID terakhir dari halaman sebelumnya             |
| limit     | integer | Tidak | 10       | Jumlah review per halaman                       |

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully retrieved company reviews",
  "meta": {
    "nextCursor": 90,
    "hasMore": true
  },
  "result": [
    {
      "id": 101,
      "jobTitle": "Backend Developer",
      "internshipType": "Magang",
      "workScheme": "WFH",
      "year": 2025,
      "duration": 3,
      "ratings": {
        "workCulture": 4,
        "learningOpp": 5,
        "mentorship": 4,
        "benefit": 3,
        "workLifeBalance": 4
      },
      "testimony": "Pengalaman magang yang sangat berharga.",
      "pros": "Mentor responsif.",
      "cons": "Deadline ketat.",
      "submittedAt": "2026-04-02T10:45:00"
    }
  ]
}
```
