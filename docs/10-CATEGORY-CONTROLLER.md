# Category Controller

## GET /category
Mengambil semua data kategori dari database.

### Parameter
- `IncludeSubCategories` (int, optional, default: 1)
  - Jika 1/yes, response akan berisi mapping subcategories untuk setiap category.
  - Jika 0/no, hanya data kategori utama yang diambil.

### Response
```json
{
  "success": true,
  "message": "Successfully Get Categories data",
  "result": [
    {
      "categoryId": 1,
      "categoryName": "Technology",
      "subCategories": [
        { "subCategoryId": 1, "subCategoryName": "Software Development" },
        { "subCategoryId": 2, "subCategoryName": "Cloud Computing" },
        { "subCategoryId": 3, "subCategoryName": "Artificial Intelligence" },
        { "subCategoryId": 4, "subCategoryName": "Cybersecurity" },
        { "subCategoryId": 5, "subCategoryName": "Data Engineering" }
      ]
    },
    {
      "categoryId": 2,
      "categoryName": "Finance",
      "subCategories": [
        { "subCategoryId": 6, "subCategoryName": "Banking" },
        { "subCategoryId": 7, "subCategoryName": "Fintech" },
        { "subCategoryId": 8, "subCategoryName": "Investment" },
        { "subCategoryId": 9, "subCategoryName": "Insurance" },
        { "subCategoryId": 10, "subCategoryName": "Payment Systems" }
      ]
    },
    // ... kategori lain
  ]
}
```

Jika `IncludeSubCategories=0`, maka response:
```json
{
  "success": true,
  "message": "Successfully Get Categories data",
  "result": [
    { "categoryId": 1, "categoryName": "Technology" },
    { "categoryId": 2, "categoryName": "Finance" },
    // ... kategori lain
  ]
}
```

### Contoh Request
```
GET /category?IncludeSubCategories=1
GET /category?IncludeSubCategories=0
```

### Catatan
- Endpoint ini mengambil data dari tabel `categories` dan `sub_categories` sesuai relasi dan seed data pada DDL-Skripsi.sql.
- Subkategori hanya muncul jika parameter `IncludeSubCategories` bernilai 1 (default).
- Data sudah terstruktur sesuai kebutuhan frontend dan relasi database.
