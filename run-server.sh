#!/bin/bash

echo "🔨 Compiling BMS Server..."
javac -d bin -cp "lib/postgresql-42.7.13.jar" $(find src -name "*.java")

if [ $? -eq 0 ]; then
    echo "🚀 Running Bank HTTP Server..."
    java -cp "bin:lib/postgresql-42.7.13.jar" server.BankHttpServer
else
    echo "❌ Compilation failed! Fix the errors above."
fi
