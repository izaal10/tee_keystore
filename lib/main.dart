import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Keystore Encryption',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  static const platform = MethodChannel('com.example.app/keystore');
  String _encryptedText = '';
  String _decryptedText = '';

  Future<void> _generateKeyPair() async {
    try {
      await platform.invokeMethod('generateKeyPair');
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Keypair generated successfully')),
      );
    } catch (e) {
      print('Error generating keypair: $e');
    }
  }

  Future<void> _encryptText(String text) async {
    try {
      final encrypted = await platform.invokeMethod('encryptText', {"text": text});
      setState(() {
        _encryptedText = encrypted;
      });
    } catch (e) {
      print('Error encrypting text: $e');
    }
  }

  Future<void> _decryptText() async {
    try {
      final decrypted = await platform.invokeMethod('decryptText', {"encryptedText": _encryptedText});
      setState(() {
        _decryptedText = decrypted;
      });
    } catch (e) {
      print('Error decrypting text: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Keystore Encryption')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              decoration: const InputDecoration(labelText: 'Text to Encrypt'),
              onSubmitted: (text) => _encryptText(text),
            ),
            const SizedBox(height: 10),
            Text('Encrypted: $_encryptedText'),
            const SizedBox(height: 10),
            ElevatedButton(
              onPressed: _decryptText,
              child: const Text('Decrypt'),
            ),
            const SizedBox(height: 10),
            Text('Decrypted: $_decryptedText'),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _generateKeyPair,
              child: const Text('Generate Keypair'),
            ),
          ],
        ),
      ),
    );
  }
}
