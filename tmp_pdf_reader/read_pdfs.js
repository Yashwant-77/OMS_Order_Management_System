const fs = require('fs');
const pdf = require('pdf-parse');

async function readPdfs() {
    const files = [
        '../supports_documents/db_design.pdf',
        '../supports_documents/epic_document.pdf',
        '../supports_documents/project_backlog.pdf',
        '../supports_documents/project_general_guidlines.pdf',
        '../supports_documents/project_guidline_oms.pdf'
    ];

    for (const file of files) {
        try {
            console.log(`\n\n--- CONTENT OF ${file} ---\n\n`);
            const dataBuffer = fs.readFileSync(file);
            const data = await pdf(dataBuffer);
            console.log(data.text);
        } catch (e) {
            console.error(`Failed to read ${file}: ${e.message}`);
        }
    }
}

readPdfs();
