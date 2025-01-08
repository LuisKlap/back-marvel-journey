db.createCollection("users", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["email", "passwordHash", "termsAcceptedAt", "status"],
            properties: {
                email: {
                    bsonType: "string",
                    description: "O email deve ser uma string válida"
                },
                passwordHash: {
                    bsonType: "string",
                    description: "Hash da senha"
                },
                termsAcceptedAt: {
                    bsonType: "date",
                    description: "Data de aceitação dos termos"
                },
                newsletterConsent: {
                    bsonType: "bool",
                    description: "Consentimento para receber emails promocionais"
                },
                status: {
                    enum: ["active", "inactive", "banned"],
                    description: "Estado atual do usuário"
                }
            }
        }
    }
});