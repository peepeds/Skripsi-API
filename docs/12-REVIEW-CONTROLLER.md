# Review Controller Documentation

Base URL: `/review`

---

## 1. Submit Internship Review

**Endpoint:** `POST /review/{slug}`

**Access:** Autentikasi diperlukan (User login)

**Deskripsi:** Mengajukan review pengalaman magang baru. Endpoint ini membuat internship header (data magang utama), internship detail (rincian review), dan recruitment steps (tahapan proses rekrutmen). Perusahaan ditentukan melalui URL path parameter `slug` (company slug).

### Path Parameters

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| slug | String | Ya | Company slug (misal: "gojek", "tokopedia", dsb.) |

### Request Body

```json
{
  "internshipType": "FULL_TIME",
  "workScheme": "ONSITE",
  "duration": 3,
  "year": 2024,
  "jobTitle": "Software Engineer",
  "SubCategoryIds": [12, 15, 18],
  "ratings": {
    "workCulture": 5,
    "learningOpp": 4,
    "mentorship": 5,
    "benefit": 3,
    "workLifeBalance": 4
  },
  "recruitmentProcess": [
    "Online Test",
    "Technical Interview",
    "HR Interview"
  ],
  "interviewDifficulty": 3,
  "testimony": "Pengalaman magang yang sangat berharga. Mentor sangat supportif dan environment kerja yang positif.",
  "pros": "Durasi 3 bulan cukup untuk memahami core business dan implementasi project nyata.",
  "cons": "Proses onboarding bisa lebih terstruktur dan dokumentasi teknis perlu dilengkapi."
}
```

### Request Fields

| Field | Type | Required | Validasi | Deskripsi |
|-------|------|----------|----------|-----------|
| internshipType | String | Ya | Maks 10 karakter | Tipe magang (misal: FULL_TIME, PART_TIME, dsb.) |
| workScheme | String | Ya | Maks 10 karakter | Skema kerja (misal: ONSITE, REMOTE, HYBRID) |
| duration | Number | Ya | > 0 | Durasi magang dalam bulan |
| year | Number | Ya | >= 2000 | Tahun mulai magang |
| jobTitle | String | Ya | Maks 75 karakter | Nama posisi/job title yang dilamar |
| SubCategoryIds | Array[Number] | Tidak | Maks 5 items | Array ID sub-kategori tambahan (skills/expertise tambahan yang digunakan di job). Maksimal 5 item. |
| ratings | Object | Ya | Nested validation | Objek berisi 5 rating (lihat sub-section) |
| ratings.workCulture | Number | Ya | 0-5 | Rating budaya kerja (0-5 bintang) |
| ratings.learningOpp | Number | Ya | 0-5 | Rating peluang belajar (0-5 bintang) |
| ratings.mentorship | Number | Ya | 0-5 | Rating mentorship (0-5 bintang) |
| ratings.benefit | Number | Ya | 0-5 | Rating benefit/kompensasi (0-5 bintang) |
| ratings.workLifeBalance | Number | Ya | 0-5 | Rating work-life balance (0-5 bintang) |
| recruitmentProcess | Array[String] | Tidak | - | Array tahapan proses rekrutmen |
| interviewDifficulty | Number | Ya | 0-5 | Tingkat kesulitan wawancara (0-5 level) |
| testimony | String | Ya | 1-500 karakter | Testimoni pengalaman magang |
| pros | String | Ya | 1-500 karakter | Keuntungan/aspek positif dari pengalaman magang |
| cons | String | Ya | 1-500 karakter | Kelemahan/aspek yang perlu ditingkatkan |

### Example Request

```bash
curl -X POST "http://localhost:8080/review/gojek" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "internshipType": "FULL_TIME",
    "workScheme": "ONSITE",
    "duration": 3,
    "year": 2024,
    "jobTitle": "Software Engineer",
    "SubCategoryIds": [12, 15, 18],
    "ratings": {
      "workCulture": 5,
      "learningOpp": 4,
      "mentorship": 5,
      "benefit": 3,
      "workLifeBalance": 4
    },
    "recruitmentProcess": ["Online Test", "Technical Interview", "HR Interview"],
    "interviewDifficulty": 3,
    "testimony": "Pengalaman magang yang sangat berharga.",
    "pros": "Durasi 3 bulan cukup untuk memahami core business.",
    "cons": "Proses onboarding bisa lebih terstruktur."
  }'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Review submitted successfully",
  "result": {
    "internshipHeaderId": 42,
    "internshipDetailId": 42,
    "createdAt": "2024-03-22T15:30:45.123+00:00",
    "message": "Review submitted successfully"
  }
}
```

### Response Fields

| Field | Type | Deskripsi |
|-------|------|-----------|
| success | Boolean | Status keberhasilan request |
| message | String | Pesan status |
| result.internshipHeaderId | Long | ID internship header yang baru dibuat |
| result.internshipDetailId | Long | ID internship detail yang baru dibuat |
| result.createdAt | String | Timestamp pembuatan (ISO 8601 format) |
| result.message | String | Pesan konfirmasi |

---

## Data Flow

### Tabel yang Dibuat/Dimodifikasi

Request akan membuat data di 3 tabel dan 1 audit log:

1. **internship_headers**
   - user_id: Diambil dari user yang login
   - company_id: Dari path parameter `slug` (dikonversi ke company ID melalui lookup di database)
   - job_title: Dari payload `jobTitle`
   - start_year: Dari payload `year` (dikonversi ke DATE format: 1 Januari tahun tersebut)
   - duration_months: Dari payload `duration`
   - created_at: Waktu saat ini
   - created_by: User ID yang login
   - Constraint: UNIQUE(user_id, company_id) — User hanya bisa membuat 1 internship per perusahaan

2. **internship_details** (1:1 dengan internship_headers)
   - internship_header_id: FK ke internship_headers
   - type: Dari payload `internshipType`
   - scheme: Dari payload `workScheme`
   - work_culture_rating: Dari payload `ratings.workCulture`
   - learning_opportunity_rating: Dari payload `ratings.learningOpp`
   - mentorship_rating: Dari payload `ratings.mentorship`
   - benefits_rating: Dari payload `ratings.benefit`
   - work_life_balance_rating: Dari payload `ratings.workLifeBalance`
   - interview_difficulty_rating: Dari payload `interviewDifficulty`
   - testimony: Dari payload `testimony`
   - pros: Dari payload `pros`
   - cons: Dari payload `cons`
   - created_at: Waktu saat ini
   - created_by: User ID yang login

3. **recruitment_steps** (many:1 dengan internship_headers)
   - internship_header_id: FK ke internship_headers
   - step_name: Setiap item dari array `recruitmentProcess`
   - Satu row per array item (misal: 3 item dalam array = 3 rows)

4. **audit_logs** (automatically created)
   - entity_type: "INTERNSHIP_REVIEW"
   - entity_id: internship_header_id
   - action: "SUBMITTED"
   - actor_id: User yang submit
   - timestamp: Waktu saat ini

---

## Error Responses

### 400 Bad Request - Validation Error

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "ratings.workCulture": "must be between 0 and 5"
  }
}
```

**Penyebab umum validation error:**
- Rating fields bukan angka atau diluar range 0-5
- `internshipType` atau `workScheme` terlalu panjang (> 10 karakter)
- `testimony`, `pros`, atau `cons` melebihi 500 karakter atau kosong
- `jobTitle` melebihi 75 karakter atau kosong
- `year` kurang dari 2000

### 404 Not Found - Company Slug Invalid

```json
{
  "success": false,
  "message": "Company with slug 'invalid-slug' not found"
}
```

**Penyebab:**
- Path parameter `slug` tidak sesuai dengan company slug yang ada di database

### 400 Bad Request - Business Logic Error

```json
{
  "success": false,
  "message": "User already has an internship for this company"
}
```

**Penyebab:**
- User sudah pernah membuat review untuk perusahaan yang sama (UNIQUE constraint pada user_id, company_id)
- Job ID atau Company ID tidak ditemukan di database

### 401 Unauthorized

```json
{
  "success": false,
  "message": "User must be authenticated"
}
```

**Penyebab:** Token tidak valid atau tidak disertakan dalam Authorization header

### 500 Internal Server Error

```json
{
  "success": false,
  "message": "Internal server error"
}
```

**Penyebab:** Error di server side (database error, transaction rollback, etc.)

---

## Implementation Notes

### Transaction Handling
- Semua create operations (internship_headers, internship_details, recruitment_steps, audit_logs) berjalan dalam **satu transaction**
- Jika ada error saat insert, **semua data akan di-rollback** (atomicity)
- Contoh: Jika recruitment_steps gagal disave, maka internship_headers dan internship_details juga akan dihapus

### Field Mapping
| Payload Field | Target Column | Table |
|---|---|---|
| companyId | company_id | internship_headers |
| internshipType | type | internship_details |
| workScheme | scheme | internship_details |
| duration | duration_months | internship_headers |
| year | start_year | internship_headers (LocalDate.of(year, 1, 1)) |
| position | job_id | internship_headers |
| SubCategoryIds | - | (not directly stored; used for validation/reference) |
| ratings.workCulture | work_culture_rating | internship_details |
| ratings.learningOpp | learning_opportunity_rating | internship_details |
| ratings.mentorship | mentorship_rating | internship_details |
| ratings.benefit | benefits_rating | internship_details |
| ratings.workLifeBalance | work_life_balance_rating | internship_details |
| recruitmentProcess[] | step_name | recruitment_steps (multiple rows) |
| interviewDifficulty | interview_difficulty_rating | internship_details |
| testimony | testimony | internship_details |
| pros | pros | internship_details |
| cons | cons | internship_details |

### Category & Subcategory
- Tidak disimpan langsung di internship_details
- Category dan subcategory **diakses melalui relasi job** (position/job_id → jobs.category_id, jobs.sub_category_id)
- Untuk query review dengan kategori, perlu join melalui job_id ke jobs table

### Nullable Fields
- `recruitmentProcess` boleh null atau empty array — jika ada items, akan membuat recruitment_steps rows

---

## Related Entities

### Job (untuk field `position`)
- **Table:** jobs
- **Primary Key:** job_id
- **Required References:**
  - category_id (FK ke categories table)
  - sub_category_id (FK ke sub_categories table)

### Company (untuk field `companyId`)
- **Table:** companies
- **Primary Key:** company_id
- **Constraint:** Harus ada di database

### User (implicit)
- **Table:** users
- **Primary Key:** user_id
- **Auto-populated:** Diambil dari authentication token

---

## Example Workflow

1. **User login** dan mendapat authentication JWT token
2. **User submit POST /review** dengan data lengkap beserta Bearer token
3. **Server validate:** semua fields sesuai schema dan constraint
4. **Jika valid:**
   - Save InternshipHeader dengan user_id, company_id, job_id, duration, year
   - Save InternshipDetail dengan ratings, testimonies, type, scheme
   - Loop dan save RecruitmentStep untuk setiap item dalam recruitmentProcess
   - Create audit log dengan entity_type "INTERNSHIP_REVIEW"
   - Return success response dengan internship IDs dan timestamp

5. **Jika ada error di step manapun:**
   - Rollback semua changes (atomicity)
   - Return error response dengan detail
