(function(){
    const srcDefaultLanguage = sessionStorage.getItem("srcDefaultLanguage") || "nb";
    const dstDefaultLanguage = sessionStorage.getItem("srcDefaultLanguage") || "en";
    const fullOrSingleDefault = sessionStorage.getItem("fullOrSingleDefault") || "f";
    const defaultSeparator = sessionStorage.getItem("fullOrSingleDefault") || "1.";
    const defaultServer = "https://localhost:8443";

    const dictionaryOfControlWords = {
          en: ['Flowers','Bird','Evening'],
          uk: ['Квіти','Птах','Вечір'],  
          nb: ['Blomster','Fugl','Kveld'],  
          nn: ['Blomstrar','Fugl','Kveld'],  
          de: ['Blumen','Vogel','Abend'],  
          pl: ['Kwiaty','Ptak','Wieczór'],  
          it: ['Fiori','Uccello','Sera'],  
          es: ['Flores','Pájaro','Tarde'],  
          fr: ['Fleurs','Oiseau','Soir'],  
          pt: ['Flores','Pássaro','Noite'],  
          ru: ['Цветы','Птица','Вечер'],  
          sv: ['Blommor','Fågel','Afton'],  
          da: ['Blomster','Fugl','Aften'],  
          gr: ['Λουλούδια','Πουλί','Εσπερινός'],  
          cz: ['Květiny','Pták','Večer'],  
          bg: ['Цветя','Птица','Вечер'],  
     };   
     function calculateWordEntry(word, data) {
        const n = data.length;
        let pos = 0;
        let count = 0;
        while (pos<n) {
            pos = data.indexOf(word, pos);
            if (pos<0) {break;}
            pos += word.length;
            count++;
        }
        return count;
     }
    
     function calculateWordWithPeriodArray(words, data) {
        let count = 0;
        const n = words && words.length;
        if (n) {
            for(let i=0;i<n;i++) {
                const s = words[i];
                if (!s) {
                    continue;
                }
                const cnt = calculateWordEntry(s + ".", data);
                count += cnt;
            }
        }
        return count;
     }
    
     function calculateWordsForLanguage(lang, data) {
        return calculateWordWithPeriodArray(dictionaryOfControlWords[lang], data);
     }
    
     function conlog(mess) {
        const el = document.getElementById("messages");
        el.innerHTML += "<br/>"+mess;
        console.log(mess);
     }
     function clearlog() {
        const el = document.getElementById("messages");
        el.innerHTML = "";
       
     }
     function checkWordsForSrcAndDst(dst) {
        const srcLang = readSelectForm("srcLang", srcDefaultLanguage, 2);
        const dstLang = readSelectForm("dstLang", dstDefaultLanguage, 2); 
        // const dst = document.getElementById("dst").value.trim();
        const src = document.getElementById("src").value.trim();
        const nsrc = calculateWordsForLanguage(srcLang, src);
        const ndst = calculateWordsForLanguage(dstLang, dst);
        if (nsrc===ndst) {
            return true;
        }
        conlog(`${nsrc} count in ${srcLang} does not match ${ndst} count in ${dstLang}`);
        return false;
     }
    
     function deepCheckProperReply(dst) {
        const sep = readInputForm("sep",defaultSeparator);
        if (sep.indexOf('1')>=0) {
            return checkWordsForSrcAndDst(dst);
        }
        return true;
     }
    
     async function afterLoad(response) {
        const data = await response.json();
        document.getElementById('stat').innerHTML = data.statistics;
        const txt = (data.words || '').trim();
        document.getElementById('src').value = txt;
        document.getElementById('dst').value = "";
        copyWords(txt);
        console.log(data);
    }  
    
    function readSelectForm(elemId, defVal, valWidth) {
        const elem = document.getElementById(elemId);
        if (!elem) {
            console.log("Error: element " + elemId + " not found");
            return defVal;
        }
        let value = elem.value;
        if (!value || value.length!==valWidth) {
            console.log("Error: element " + elemId + " has wrong value "+ value);
            return defVal;
        } 
        return value; 
    }
    
    function readInputForm(elemId, defVal) {
        const elem = document.getElementById(elemId);
        if (!elem) {
            console.log("Error: element " + elemId + " not found");
            return defVal;
        }
        let value = elem.value;
        if (!value) {
            console.log("Error: element " + elemId + " has wrong value "+ value);
            return defVal;
        } 
        return value; 
    }
    
    function formUrl() {
        const srcLang = readSelectForm("srcLang", srcDefaultLanguage, 2);
        const dstLang = readSelectForm("dstLang", dstDefaultLanguage, 2); 
        const fullOrSingle = readSelectForm("fullOrSingle", fullOrSingleDefault, 1);
        const mode = readSelectForm("mode", "ma", 2);
        const lim = readInputForm("lim", "5000");
        const sep = encodeURIComponent(readInputForm("sep",defaultSeparator));
        return `${srcLang}/${dstLang}/${fullOrSingle}?limit=${lim}&separator=${sep}&mode=${mode}`;
    }
    
    function loadWords(){
        const url = defaultServer + "/api/translate/next/" + formUrl(); 
        fetch(url).then(afterLoad);
    }
    
    function copyWords(text){
        if (!text) {
            text = document.getElementById('src').value;
        }
        navigator.clipboard.writeText(text).then(function() {
            console.log('Text copied to clipboard');
        }).catch(function(error) {
            console.error('Error copying text: ', error);
        });
    }    
    
    function isValidUUID(id) {
        if (id.length!==36) {
            return false;
        }
        for(let i=0;i<36;i++) {
            const c = id[i];
            if (i===8 || i===13 || i===18 || i===18 || i===23) {
                if (c!=="-") {
                    return false;
                }
                continue;
            }
            if (!(c>='0' && c<='9' || c>='a' && c<='f' || c>='A' && c<='F')) {
                return false;
            }
        }
        return true;
    }
    
    function checkProperReply(text) {
        const orig=document.getElementById('src').value.trim();
        if (text===orig || text.length<38 || orig.length<38) {
            return false;
        }
        const id = orig.substr(0, 36);
        if (text.substr(0,36)!==id || !isValidUUID(id)) {
            return false;
        }
        return true;
    }
    
    let lastClipText = '';
    let lastTimerHandle = 0;
    
    function checkPasteWords() {
        pasteWords(0);
    }
    function checkSelectChange(e) {
        console.log(e);
        const mode = readSelectForm("mode", "ma", 2);
        if (mode==="ap") {
            lastTimeHandle = setInterval(checkPasteWords, 200);
            console.log("Automatic pasting is on");
        } else {
            clearInterval(lastTimerHandle);
            console.log("Automatic pasting is off");
        }
    }
    function pasteWords(force){
        const rd = document.getElementById("inpo");
        navigator.clipboard.readText().then((clipText) => {
           clipText = clipText.trim();
           rd.style.backgroundColor="white";
           if (!force && lastClipText===clipText) {
               return;
           }
           lastClipText = clipText;
           check = checkProperReply(clipText);
           if (check || force) {
                document.getElementById("dst").value = clipText;
           }
           if (check) {
            console.log("pnt1", clipText);
            if (deepCheckProperReply(clipText)) {
                uploadWords();
            }
           }
        }).catch(err => {
            const m = err+"";
            if (m.includes("Document is not focused.")) {
                 rd.style.backgroundColor="yellow";
            } else {
                rd.style.backgroundColor="red";
                console.error('Failed to read clipboard contents: ', err);
            }
        });
    }    
    
    async function uploadWords(){
        const s = document.getElementById("dst").value.trim();
        const id = s.substr(0, 36);
        if (!isValidUUID(id)) {
            return false;
        }
        let url = defaultServer + "/api/translate/part/"+formUrl();
        const response = await fetch(url, {
           method: 'POST', // *GET, POST, PUT, DELETE, etc.
           headers: {
             'Content-Type': 'text/html; charset=utf-8'
           },
           body: s, // body data type must match "Content-Type" header
        });
        afterLoad(response);
    }
    function dvHide() {
        const dv = document.getElementById('sova');
        dv.style.display = "none";
    }
    function dvShow() {
        const dv = document.getElementById('sova');
        dv.style.display = "block";
    }
    window.sova = dvShow; 
    const mainHtml=`
    <div id="inpo">
    <span>Origin:</span> 
    <select id="srcLang">
         <option>en</option>
         <option selected="selected">nb</option>
         <option>nn</option>
         <option>uk</option>
         <option>de</option>
         <option>pl</option>
         <option>fr</option>
         <option>ru</option>
         <option>es</option>
         <option>it</option>
         <option>da</option>
         <option>sv</option>
         <option>bg</option>
         <option>cz</option>
         <option>pt</option>
         <option>gr</option>
    </select>
    <span>Target:</span> 
    <select id="dstLang">
         <option selected="selected">en</option>
         <option>nb</option>
         <option>nn</option>
         <option>uk</option>
         <option>de</option>
         <option>pl</option>
         <option>fr</option>
         <option>ru</option>
         <option>es</option>
         <option>it</option>
         <option>da</option>
         <option>sv</option>
         <option>bg</option>
         <option>cz</option>
         <option>pt</option>
         <option>gr</option>
    </select>
    <span>Full:</span> 
    <select id="fullOrSingle">
         <option selected="selected">f</option>
         <option>s</option>
    </select>
    <span>Limit:</span>
    <input type="number" id="lim" value="5000" style="width:60px;"/> 
    <span>Separator:</span>
    <input type="text" id="sep" style="width:50px;" /> 

    <span>Mode:</span> 
    <select id="mode" >
         <option value="au" selected="selected">auto upload</option>
         <option value="ap">auto paste</option>
         <option value="ma">manual</option>
    </select>

    <button id="bt_loadWords">Load</button>
    <button id="bt_copyWords">Copy</button>
    <button id="bt_pasteWords">Paste</button>
    <button id="bt_uploadWords">Upload</button>
    <button id="bt_hide">Hide</button>
    <span id="stat"></span>
</div>
<textarea id="src" style="width: 500px;height: 200px;"></textarea>
<textarea id="dst" style="width: 500px;height: 200px;"></textarea>
<div id="messages" style="color: red" ></div>

    `;    
    const rt = document.createElement("div");
    rt.style.position = "absolute";
    rt.style.left = "0px;";
    rt.style.top = "0px";
    rt.id = 'sova';
    rt.innerHTML = mainHtml;
    setTimeout(()=>{
        document.body.appendChild(rt);
        document.getElementById("bt_loadWords").addEventListener("click",()=>loadWords());
        document.getElementById("bt_copyWords").addEventListener("click",()=>copyWords());
        document.getElementById("bt_pasteWords").addEventListener("click",()=>pasteWords(1));
        document.getElementById("bt_uploadWords").addEventListener("click",()=>uploadWords());
        document.getElementById("bt_hide").addEventListener("click",()=>dvHide());
        document.getElementById("mode").addEventListener("change",()=>checkSelectChange());
        document.getElementById("messages").addEventListener("click",()=>clearlog());
        document.getElementById("sep").value=defaultSeparator;
    }, 400);    
})();