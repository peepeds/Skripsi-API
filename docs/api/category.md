# Dokumentasi API — Kategori

Base paths: `/category`, `/subcategory`

---

## GET /category

**Autentikasi**: Publik

Mengambil daftar kategori beserta subkategorinya (opsional).

### Query Parameters

| Parameter            | Tipe    | Wajib | Default | Keterangan                                                          |
|----------------------|---------|-------|---------|---------------------------------------------------------------------|
| IncludeSubCategories | integer | Tidak | 1       | `1` untuk menyertakan subkategori, `0` untuk mengecualikan          |
| type                 | string  | Tidak | jobs    | Jenis kategori yang diambil, contoh: `jobs`, `companies`            |

### Contoh Response — Dengan Subkategori

```json
{
  "success": true,
  "message": "Successfully Get Categories data",
  "result": [
    {
      "id": 1,
      "categoryName": "Teknologi",
      "subCategories": [
        {
          "id": 7,
          "subCategoryName": "Software Development"
        },
        {
          "id": 8,
          "subCategoryName": "Data Science"
        }
      ]
    }
  ]
}
```

### Contoh Response — Tanpa Subkategori (`IncludeSubCategories=0`)

```json
{
  "success": true,
  "message": "Successfully Get Categories data",
  "result": [
    {
      "id": 1,
      "categoryName": "Teknologi"
    }
  ]
}
```

---

## GET /subcategory/{subCategoryName}/companies

**Autentikasi**: Publik

Mengambil daftar perusahaan yang terkait dengan suatu subkategori, berdasarkan nama subkategori.

### Path Parameters

| Parameter       | Tipe   | Keterangan                            |
|-----------------|--------|---------------------------------------|
| subCategoryName | string | Nama subkategori (URL-encoded jika mengandung spasi) |

### Query Parameters

| Parameter | Tipe    | Wajib | Default   | Keterangan                                    |
|-----------|---------|-------|-----------|-----------------------------------------------|
| type      | string  | Tidak | companies | Tipe pencarian                                |
| cursor    | long    | Tidak | —         | ID terakhir dari halaman sebelumnya           |
| limit     | integer | Tidak | 10        | Jumlah data per halaman                       |

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Get Companies by SubCategory data",
  "meta": {
    "nextCursor": 25,
    "hasMore": true
  },
  "result": [
    {
      "id": 5,
      "companyName": "PT Teknologi Maju",
      "slug": "pt-teknologi-maju",
      "isPartner": true,
      "averageRating": 4.8
    }
  ]
}
```
