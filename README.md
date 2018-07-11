# stc-speechkit-android
<p>Use the STC SpeechKit library to integrate speech recognition, text-to-speech and diarization.</p>
<h2>System Requirements</h2>
<ol>
<li>Minimum required Android platform version: 4.1 (API Level 16)</li>
<li>Internet Connection</li>
</ol>
<h2>Before you get started</h2>
<ol>
<li>Review the <a href="https://stc-voicekey.github.io/stc-speechkit-android/index.html" rel="nofollow">Documentation</a> for the library API</li>
<li>Register on the <a href="https://cp.speechpro.com/" rel="nofollow">website</a> and get credentials</li>
</ol>
<h2>Integration</h2>
<p>Download the <a href="https://github.com/STC-VoiceKey/stc-speechkit-android/" rel="nofollow">project</a> and compile from the sources or use Gradle:</p>

        dependencies {
            ...   
            implementation 'com.speechpro.android:stcspeechkit:0.8.2'        
        }
        
<h2>How to use STC SpeechKit</h2>
<h5>Initialization&nbsp;STCSpeechKit</h5>

    STCSpeechKit.init(activity!!.applicationContext, login, password, domainId.toInt())
    
<h5>Enabling&nbsp;logging</h5>
    
    STCSpeechKit.setLogging(true)
    
<h5>WebSocketRecognizer</h5>

    webSocketRecognizer = WebSocketRecognizer.Builder(this)
            .build()
                   
    webSocketRecognizer.startRecording()
    webSocketRecognizer.stopRecording() 
        
    override fun onRecognizerTextMessage(result: String) {
        //intermediate result
    }
        
    override fun onRecognizerTextResult(result: String) {
        //final result
    }  
    
<h5>RestApiRecognizer</h5>

    restApiRecognizer = RestApiRecognizer.Builder(this)
            .build()
                      
    webSocketRecognizer.startRecording()
    webSocketRecognizer.stopRecording() 
           
    override fun onRecognizerTextMessage(result: String) {
       //intermediate result
    }
        
    override fun onRecognizerTextResult(result: String) {
       //final result
    }  
    
<h5>WebSocketSynthesizer</h5>

    webSocketSynthesizer = WebSocketSynthesizer.Builder(this)
                .language(Language.Russian)
                .speaker("Alexander")
                .build()
                
    webSocketSynthesizer.synthesize("Тестовый синтез")
               
    override fun onSynthesizerResult(byteArray: ByteArray) {
        //PCM
    }
    
<h5>RestApiSynthesizer</h5>

    restApiSynthesizer = RestApiSynthesizer.Builder(this)
                .language(Language.Russian)
                .speaker("Alexander")
                .build()
                
    restApiSynthesizer.synthesize("Тестовый синтез")
               
    override fun onSynthesizerResult(byteArray: ByteArray) {
        //WAV
    }
        
<h5>RestApiDiarization</h5>

    diarization = RestApiDiarization.Builder(this)
            .build()
            
    diarization.startRecording()
    diarization.stopRecording() 
                
    override fun onDiarizationResult(result: Data) {
        //result
    }
    
<h2><a id="user-content-license" class="anchor" aria-hidden="true" href="#license"><svg class="octicon octicon-link" viewBox="0 0 16 16" version="1.1" width="16" height="16" aria-hidden="true"><path fill-rule="evenodd" d="M4 9h1v1H4c-1.5 0-3-1.69-3-3.5S2.55 3 4 3h4c1.45 0 3 1.69 3 3.5 0 1.41-.91 2.72-2 3.25V8.59c.58-.45 1-1.27 1-2.09C10 5.22 8.98 4 8 4H4c-.98 0-2 1.22-2 2.5S3 9 4 9zm9-3h-1v1h1c1 0 2 1.22 2 2.5S13.98 12 13 12H9c-.98 0-2-1.22-2-2.5 0-.83.42-1.64 1-2.09V6.25c-1.09.53-2 1.84-2 3.25C6 11.31 7.55 13 9 13h4c1.45 0 3-1.69 3-3.5S14.5 6 13 6z"></path></svg></a>License</h2>
<p>Copyright (c) 2018 STC. Licensed under the FreeBSD <a href="https://onepass.tech/license-agreement.html" rel="nofollow">License</a>.</p>






