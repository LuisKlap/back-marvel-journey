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
                },
                createdAt: {
                    bsonType: "date",
                    description: "Data de criação do usuário"
                },
                updatedAt: {
                    bsonType: "date",
                    description: "Data da última atualização do usuário"
                },
                loginAttempts: {
                    bsonType: "object",
                    properties: {
                        count: {
                            bsonType: "int",
                            description: "Número de tentativas de login"
                        },
                        lastAttemptAt: {
                            bsonType: "date",
                            description: "Data da última tentativa de login"
                        }
                    }
                },
                roles: {
                    bsonType: "array",
                    items: {
                        bsonType: "string",
                        enum: ["ROLE_USER", "ROLE_ADMIN", "ROLE_MASTER"]
                    },
                    description: "Lista de papéis do usuário"
                },
                metadata: {
                    bsonType: "object",
                    properties: {
                        lastLoginAt: {
                            bsonType: "date",
                            description: "Data do último login"
                        },
                        ipAddress: {
                            bsonType: "string",
                            description: "Endereço IP do último login"
                        },
                        userAgent: {
                            bsonType: "string",
                            description: "User agent do último login"
                        },
                        devices: {
                            bsonType: "array",
                            items: {
                                bsonType: "string"
                            },
                            description: "Lista de dispositivos do usuário"
                        }
                    }
                },
                mfa: {
                    bsonType: "object",
                    properties: {
                        secret: {
                            bsonType: "string",
                            description: "Segredo do MFA"
                        },
                        enabled: {
                            bsonType: "bool",
                            description: "Indica se o MFA está habilitado"
                        }
                    }
                },
                failedLoginAttempts: {
                    bsonType: "int",
                    description: "Número de tentativas de login falhadas"
                },
                lockoutEndTime: {
                    bsonType: "date",
                    description: "Data e hora do fim do bloqueio de conta"
                },
                isTest: {
                    bsonType: "bool",
                    description: "Indica se o registro é de teste"
                },
                verificationCode: {
                    bsonType: "object",
                    properties: {
                        emailIsVerified: {
                            bsonType: "bool",
                            description: "Indica se o email foi verificado"
                        },
                        code: {
                            bsonType: "string",
                            description: "Código de verificação"
                        },
                        createdAt: {
                            bsonType: "date",
                            description: "Data de criação do código de verificação"
                        }
                    }
                }
            }
        }
    }
});