import fitz  # PyMuPDF
import glob
import os

pdf_dir = 'supports_documents'
pdfs = glob.glob(os.path.join(pdf_dir, '*.pdf'))

with open('pdf_contents.txt', 'w', encoding='utf-8') as f:
    for pdf in pdfs:
        f.write(f"\n\n--- CONTENT OF {os.path.basename(pdf)} ---\n\n")
        try:
            doc = fitz.open(pdf)
            for page in doc:
                f.write(page.get_text())
        except Exception as e:
            f.write(f"Error reading {pdf}: {e}\n")
