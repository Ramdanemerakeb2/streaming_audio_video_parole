
'use strict'; 

var express = require('express');
var app = express();
var server = require('http').createServer(app);
var globalres;
var vocalTranscription;

server.listen(3000, function () {
  console.log('Server listening at port 3000');
});

var fs= require('fs');
var chunks=[];
var path='audio.wav';

app.get('/', function (req, res) {
    res.send('Hello world!');
});

app.get('/audio',function(req,res){
    res.sendFile(__dirname+'/audio.wav');
});

app.post('/upload',function(req,res){
     chunks=[];
     req.on('data',function(chunk){
     	chunks.push(chunk);
     
     });
     
     req.on('end',function(){
     	var data=Buffer.concat(chunks);
	fs.writeFile(path,data,'binary',function(err){
		if(err){
			console.log('couldnt make file'+err);
			res.send('erreur de conversion du fichier')
		}
		else{
			console.log("Audio Recieved:");
			console.log(data);
			(async() => {
				vocalTranscription = await syncRecognize();
				console.log(vocalTranscription);
				res.send(vocalTranscription)
			})()	
		}
	});
     });
});

async function syncRecognize()
{
	// Imports the Google Cloud client library
	const fs = require('fs');
	const speech = require('@google-cloud/speech');

	// Creates a client
	const client = new speech.SpeechClient();

	//const filename = "C:/Users/appleworld/Desktop/Eng.wav";
	const encoding = 'LINEAR16';
	const sampleRateHertz = 16000;
	const languageCode = 'fr-FR';

	const config = {
	  encoding: encoding,
	  sampleRateHertz: sampleRateHertz,
	  languageCode: languageCode,
	  audioChannelCount : 1,
	};

	const audio = {
	  content: fs.readFileSync(path).toString('base64'),
	};

	const request = {
	  config: config,
	  audio: audio,
	};

	// Detects speech in the audio file
	const [response] = await client.recognize(request);
	const transcription = response.results
	  .map(result => result.alternatives[0].transcript)
	  .join('\n');
	console.log('Transcription: ', transcription);

	return await transcription;
}
