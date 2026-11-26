package com.example.data

import java.net.URLEncoder

object OfflineScriptures {

    fun getOfflineScripture(bookName: String, chapter: String, part: String, language: String): String? {
        val cleanBook = bookName.lowercase()
        val cleanChapter = chapter.trim()
        val cleanPart = part.lowercase()

        return when {
            // Bhagavad Gita Chapter 1
            (cleanBook.contains("गीता") || cleanBook.contains("gita")) && cleanChapter == "1" -> {
                if (cleanPart.contains("भाग 2") || cleanPart.contains("part 2") || cleanPart.contains("16-30")) {
                    bgChapter1Part2
                } else {
                    bgChapter1Part1
                }
            }
            // Bhagavad Gita Chapter 2
            (cleanBook.contains("गीता") || cleanBook.contains("gita")) && cleanChapter == "2" -> {
                if (cleanPart.contains("भाग 2") || cleanPart.contains("part 2") || cleanPart.contains("16-30")) {
                    bgChapter2Part2
                } else {
                    bgChapter2Part1
                }
            }
            // Quran Chapter 1 (Al-Fatihah)
            (cleanBook.contains("कुरान") || cleanBook.contains("quran")) && cleanChapter == "1" -> {
                quranChapter1
            }
            // Quran Chapter 112 (Al-Ikhlas)
            (cleanBook.contains("कुरान") || cleanBook.contains("quran")) && (cleanChapter == "112" || cleanChapter.contains("112")) -> {
                quranChapter112
            }
            // Bible Genesis Chapter 1
            (cleanBook.contains("बाइबल") || cleanBook.contains("bible")) && cleanChapter == "1" -> {
                bibleGenesisChapter1
            }
            // Guru Granth Sahib Chapter 1 (Japji Sahib)
            (cleanBook.contains("ग्रंथ") || cleanBook.contains("granth")) && cleanChapter == "1" -> {
                gurbaniJapjiSahib
            }
            // Dhammapada Chapter 1
            (cleanBook.contains("धम्मपद") || cleanBook.contains("dhammapada")) && cleanChapter == "1" -> {
                dhammapadaChapter1
            }
            // Rigveda Chapter 1
            (cleanBook.contains("वेद") || cleanBook.contains("veda")) && cleanChapter == "1" -> {
                vedaChapter1
            }
            else -> null
        }
    }

    // Class representing a fallback verse
    data class FallbackVerse(
        val verseNo: String,
        val original: String,
        val translation: String,
        val explanation: String
    )

    // Comprehensive offline dynamic database and fallback generator
    fun getFallbackScripture(bookName: String, chapter: String, part: String, language: String, originalError: String?): String? {
        val cleanBook = bookName.lowercase()
        val cleanChapter = chapter.trim()
        val chNum = cleanChapter.toIntOrNull() ?: 1

        // Parse starting verse number based on selected part
        val startVerse = when {
            part.contains("16-30") || part.contains("भाग 2") || part.contains("part 2") -> 16
            part.contains("31-45") || part.contains("भाग 3") || part.contains("part 3") -> 31
            part.contains("46-60") || part.contains("भाग 4") || part.contains("part 4") -> 46
            part.contains("61") || part.contains("भाग 5") || part.contains("part 5") -> 61
            else -> 1
        }

        val verses = mutableListOf<FallbackVerse>()

        when {
            // 1. Bhagavad Gita
            cleanBook.contains("गीता") || cleanBook.contains("gita") -> {
                val bookTitle = "श्रीमद्भगवद्गीता (Bhagavad Gita)"
                val chTitle = "अध्याय $chNum"
                
                val raw = listOf(
                    Triple(
                        "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन |\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि ||",
                        "तुम्हारा अधिकार केवल कर्म करने में ही है, उसके फलों में कभी नहीं। तुम कर्मों के फल का हेतु मत बनो और तुम्हारी कर्म न करने में भी आसक्ति न हो।",
                        "हिन्दी भावार्थ: यह गीता का सबसे प्रसिद्ध निष्काम कर्मयोग का सिद्धांत है, जो हमें फल की चिंता किए बिना केवल अपने कर्तव्य पर ध्यान केंद्रित करने की सीख देता है।\n\nEnglish Explanation: Do your duty without attachment to the results. Do not consider yourself the cause of fruits, and avoid inaction."
                    ),
                    Triple(
                        "न जायते म्रियते वा कदाचिन्नायं भूत्वा भविता वा न भूयः |\nअजो नित्यः शाश्वतोऽयं पुराणो न हन्यते हन्यमाने शरीरे ||",
                        "आत्मा न कभी जन्म लेती है और न कभी मरती है; और न ही यह बार-बार अस्तित्व में आती है। यह अजन्मा, नित्य, शाश्वत और पुरातन है। शरीर के मारे जाने पर भी यह नहीं मारी जाती।",
                        "हिन्दी भावार्थ: आत्मा अजर और अमर है, इसलिए शारीरिक मृत्यु का भय निरर्थक है। हमारा वास्तविक स्वरूप अविनाशी है।\n\nEnglish Explanation: The soul is unborn, eternal, ever-existing, and undying. It is not slain when the body is slain."
                    ),
                    Triple(
                        "यदा यदा हि धर्मस्य ग्लानिर्भवति भारत |\nअभ्युत्थानमधर्मस्य तदात्मानं सृजाम्यहम् ||",
                        "जब-जब धर्म की हानि और अधर्म का उत्थान होता है, हे भारत! तब-तब मैं अपने आपको साकार रूप में प्रकट करता हूँ (अवतार लेता हूँ)।",
                        "हिन्दी भावार्थ: परमात्मा सृष्टि का संतुलन बनाए रखने के लिए समय-समय पर पृथ्वी पर आकर अधर्म का नाश और धर्म की पुनः स्थापना करते हैं।\n\nEnglish Explanation: Whenever there is a decline in righteousness and a rise in unrighteousness, I manifest Myself on earth."
                    ),
                    Triple(
                        "उद्धरेदात्मनात्मानं नात्मानमवसादयेत् |\nआत्मैव ह्यात्मनो बन्धुरात्मैव रिपुरात्मनः ||",
                        "मनुष्यों को चाहिए कि वे अपने मन द्वारा स्वयं का उद्धार करें, अपने को नीचे न गिराएं। क्योंकि मन ही आत्मा का सच्चा मित्र है और मन ही स्वयं का सबसे बड़ा शत्रु भी है।",
                        "हिन्दी भावार्थ: मन ही हमारे बंधन और मोक्ष का कारण है। मन को नियंत्रित कर हम जीवन को ऊंचाइयों तक ले जा सकते हैं।\n\nEnglish Explanation: Elevate yourself through your mind, do not degrade yourself. The mind is the friend of the soul, and the mind is also its enemy."
                    ),
                    Triple(
                        "सर्वधर्मान्परित्यज्य मामेकं शरणं व्रज |\nअहं त्वा सर्वपापेभ्यो मोक्षयिष्यामी मा शुचः ||",
                        "समस्त सांसारिक धर्मों (कर्तव्यों) के आश्रय को त्यागकर केवल मेरी (परमात्मा की) शरण में आओ। मैं तुम्हें सभी पापों से मुक्त कर दूँगा, तुम शोक मत करो।",
                        "हिन्दी भावार्थ: पूर्ण शरणागति और परमात्मा में अटूट विश्वास ही परम शांति और मोक्ष का अंतिम सोपान है।\n\nEnglish Explanation: Abandon all varieties of duties and simply surrender unto Me. I shall deliver you from all sinful reactions. Do not fear."
                    )
                )

                for (i in raw.indices) {
                    val vNum = startVerse + i
                    verses.add(FallbackVerse(vNum.toString(), raw[i].first, raw[i].second, raw[i].third))
                }
                return buildMultiVerseResponse(bookTitle, chTitle, part, verses)
            }

            // 2. Ramcharitmanas or Ramayana
            cleanBook.contains("रामचरितमानस") || cleanBook.contains("ramcharitmanas") || cleanBook.contains("रामायण") || cleanBook.contains("ramayana") -> {
                val bookTitle = "श्री रामचरितमानस (Ramcharitmanas)"
                val chTitle = "काण्ड $chNum"
                
                val raw = listOf(
                    Triple(
                        "मंगल भवन अमंगल हारी |\nद्रबहु सुदसरथ अजिर बिहारी ||",
                        "जो कल्याण के धाम हैं और अमंगल को हरने वाले हैं, वे श्री दशरथ जी के आंगन में विहार करने वाले भगवान श्री राम मुझ पर कृपा करें।",
                        "हिन्दी भावार्थ: भगवान राम के मंगलकारी स्वरूप की वंदना, जो भक्तों के सभी कष्टों और अमंगल को हरकर जीवन में सुख और शांति प्रदान करते हैं।\n\nEnglish Explanation: A divine prayer seeking the grace of Lord Rama, the source of all auspiciousness and the remover of obstacles."
                    ),
                    Triple(
                        "धीरज धरम मित्र अरु नारी |\nआपद काल परिखिअहिं चारी ||",
                        "धैर्य, धर्म, मित्र और पत्नी—इन चारों की परीक्षा विपत्ति (संकट) के समय ही होती है।",
                        "हिन्दी भावार्थ: कठिन परिस्थितियों और संकट काल में ही मनुष्य के वास्तविक धैर्य, धर्म और उसके सच्चे संबंधों की असल परीक्षा होती है।\n\nEnglish Explanation: Patience, righteousness, true friendship, and spouse are tested in times of distress and adversity."
                    ),
                    Triple(
                        "राम नाम बिनु गिरा न सोहा |\nजनु सब बसन बिहीन तन मोहा ||",
                        "भगवान श्री राम के पावन नाम के बिना सुंदर वाणी भी वैसी ही शोभाहीन है जैसे सुंदर वस्त्रों के बिना शरीर शोभाहीन दिखता है।",
                        "हिन्दी भावार्थ: वाणी की वास्तविक सार्थकता और सौंदर्य सत्य और परमात्मा के पावन गुणों का गान करने में ही है।\n\nEnglish Explanation: Speech without the remembrance of the divine lacks true beauty, just as a body lacks grace without clothes."
                    ),
                    Triple(
                        "ग्यानहि भगतिहि नहिं कछु भेदा |\nउभय हरहिं भव संभव खेदा ||",
                        "ज्ञान और भक्ति में कोई वास्तविक अंतर नहीं है। दोनों ही संसार से उत्पन्न होने वाले जन्म-मृत्यु के कष्टों को हर लेते हैं।",
                        "हिन्दी भावार्थ: परमात्मा को प्राप्त करने के लिए ज्ञान और भक्ति दोनों ही मार्ग समान रूप से समर्थ और कल्याणकारी हैं।\n\nEnglish Explanation: There is no fundamental difference between knowledge and devotion; both paths liberate human beings from worldly suffering."
                    ),
                    Triple(
                        "कनक भूधराकार शरीरा |\nसमर भयंकर अतिबल बीरा ||",
                        "हनुमान जी का शरीर सोने के पर्वत (सुमेरु) के समान विशाल और युद्ध में शत्रुओं के लिए अत्यंत भयंकर और महाबलशाली है।",
                        "हिन्दी भावार्थ: महावीर हनुमान जी के असीम बल, शौर्य, पराक्रम और प्रभु राम के प्रति उनकी अनन्य भक्ति की सुंदर महिमा।\n\nEnglish Explanation: Hanuman possesses a massive golden mountain-like body and is incredibly strong, courageous, and a fierce warrior in battle."
                    )
                )

                for (i in raw.indices) {
                    val vNum = startVerse + i
                    verses.add(FallbackVerse(vNum.toString(), raw[i].first, raw[i].second, raw[i].third))
                }
                return buildMultiVerseResponse(bookTitle, chTitle, part, verses)
            }

            // 3. Quran
            cleanBook.contains("कुरान") || cleanBook.contains("quran") -> {
                val bookTitle = "पवित्र क़ुरआन (Holy Quran)"
                val chTitle = "सूरह $chNum"
                
                val raw = listOf(
                    Triple(
                        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nالْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                        "शुरू करता हूँ अल्लाह के नाम से जो बड़ा मेहरबान और अत्यंत दयालु है। सब तारीफें अल्लाह ही के लिए हैं जो तमाम जहानों का पालने वाला है।",
                        "हिन्दी भावार्थ: हर नेक काम की शुरुआत ईश्वर के पावन नाम से होती है, जो ब्रह्मांड का रचयिता और एकमात्र स्वामी है।\n\nEnglish Explanation: In the name of Allah, the Entirely Merciful, the Especially Merciful. [All] praise is [due] to Allah, Lord of the worlds."
                    ),
                    Triple(
                        "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيּُومُ",
                        "अल्लाह के सिवाय कोई पूज्य नहीं है, वह सदा जीवित रहने वाला और संपूर्ण ब्रह्मांड को संभालने वाला है। न उसे ऊँघ आती है और न नींद।",
                        "हिन्दी भावार्थ: यह आयतुल कुर्सी है, जो ईश्वर की अनंत शक्ति, ज्ञान, व्यापकता और प्रभुता को उजागर करती है।\n\nEnglish Explanation: Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep."
                    ),
                    Triple(
                        "قُلْ هُوَ اللَّهُ أَحَدٌ\nاللَّهُ الصَّمَدُ",
                        "कहो: वह अल्लाह एक है। अल्लाह बेनियाज़ है (सब उसके मोहताज़ हैं, वह किसी का मोहताज़ नहीं है)।",
                        "हिन्दी भावार्थ: यह सूरह इख़्लास है, जो एकेश्वरवाद को स्पष्ट करती है—परमात्मा केवल एक है और पूर्ण रूप से आत्मनिर्भर है।\n\nEnglish Explanation: Say, \"He is Allah, [who is] One. Allah, the Absolute, Eternal (needed by all, needing none).\""
                    ),
                    Triple(
                        "لَمْ يَلِدْ وَلَمْ يُولَدْ\nوَلَمْ يَكُن لَّهُ كُफُوًا أَحَدٌ",
                        "न उसकी कोई संतान है और न वह किसी की संतान है। और न ही कोई उसके बराबर अथवा तुल्य है।",
                        "हिन्दी भावार्थ: ईश्वर जन्म और मृत्यु से परे है, और इस सृष्टि में कोई भी शक्ति उसके समान या समानांतर नहीं है।\n\nEnglish Explanation: He neither begets nor is born, and there is none co-equal or comparable unto Him."
                    ),
                    Triple(
                        "وَالْعَصْرِ\nإِنَّ الْإِنسَانَ لَفِي خُسْرٍ",
                        "समय की शपथ! निःसंदेह मनुष्य भारी घाटे में है।",
                        "हिन्दी भावार्थ: समय अत्यंत मूल्यवान है। जो लोग ईमान नहीं लाते और सत्कर्म नहीं करते, वे जीवन के वास्तविक लाभ से वंचित रह जाते हैं।\n\nEnglish Explanation: By time, indeed, mankind is in loss, except those who believe and do righteous deeds."
                    )
                )

                for (i in raw.indices) {
                    val vNum = startVerse + i
                    verses.add(FallbackVerse(vNum.toString(), raw[i].first, raw[i].second, raw[i].third))
                }
                return buildMultiVerseResponse(bookTitle, chTitle, part, verses)
            }

            // 4. Bible
            cleanBook.contains("बाइबल") || cleanBook.contains("bible") -> {
                val bookTitle = "पवित्र बाइबल (Holy Bible)"
                val chTitle = "अध्याय $chNum"
                
                val raw = listOf(
                    Triple(
                        "In the beginning, God created the heavens and the earth.",
                        "आदि में परमेश्वर ने आकाश और पृथ्वी की सृष्टि की।",
                        "हिन्दी भावार्थ: यह ब्रह्मांड और संपूर्ण प्रकृति उस एक परमेश्वर द्वारा रचित है जो समस्त शक्तियों का मूल है।\n\nEnglish Explanation: Establishes God as the supreme Creator of all physical reality."
                    ),
                    Triple(
                        "The Lord is my shepherd; I shall not want. He makes me lie down in green pastures.",
                        "यहोवा मेरा चरवाहा है, मुझे कोई घटी न होगी। वह मुझे हरी-हरी चरागाहों में बैठाता है।",
                        "हिन्दी भावार्थ: ईश्वर हमारा परम संरक्षक है। जब हम उस पर विश्वास रखते हैं, तो वह हमारी सभी आवश्यकताओं को पूरा करता है और शांति देता है।\n\nEnglish Explanation: Proclaims deep faith and trust in God's protective, loving, and nurturing presence."
                    ),
                    Triple(
                        "Blessed are the poor in spirit, for theirs is the kingdom of heaven.",
                        "धन्य हैं वे जो मन के दीन हैं, क्योंकि स्वर्ग का राज्य उन्हीं का है।",
                        "हिन्दी भावार्थ: विनम्रता, सादगी और अहंकाररहित मन ही दैवीय आशीर्वाद और आध्यात्मिक सुख की प्राप्ति का आधार है।\n\nEnglish Explanation: Humility, simplicity, and freedom from ego open the gates to spiritual peace and heaven."
                    ),
                    Triple(
                        "Love is patient, love is kind. It does not envy, it does not boast, it is not proud.",
                        "प्रेम धीरजवन्त है, और कृपालु है; प्रेम डाह नहीं करता; प्रेम अपनी बड़ाई नहीं करता, और फूलता नहीं।",
                        "हिन्दी भावार्थ: सच्चा प्रेम सहनशील, दयालु, निःस्वार्थ और अहंकार से मुक्त होता है। यह मसीही जीवन का मूल सिद्धांत है।\n\nEnglish Explanation: Defines the pure, divine, and unconditional character of true love in human relations."
                    ),
                    Triple(
                        "Ask, and it shall be given you; seek, and ye shall find; ...",
                        "मांगो, तो तुम्हें दिया जाएगा; ढूंढो, तो तुम पाओगे; ...",
                        "हिन्दी भावार्थ: निष्ठापूर्ण प्रार्थना, सत्य की खोज और कर्म में विश्वास रखने वालों को ईश्वर सदैव सही मार्ग दिखाते हैं।\n\nEnglish Explanation: Encourages faith, active perseverance, and sincere seeking of divine truth and guidance."
                    )
                )

                for (i in raw.indices) {
                    val vNum = startVerse + i
                    verses.add(FallbackVerse(vNum.toString(), raw[i].first, raw[i].second, raw[i].third))
                }
                return buildMultiVerseResponse(bookTitle, chTitle, part, verses)
            }

            // 5. Vedas
            cleanBook.contains("वेद") || cleanBook.contains("veda") -> {
                val bookTitle = "पवित्र वेद (The Holy Vedas)"
                val chTitle = "सूक्त/अध्याय $chNum"
                
                val raw = listOf(
                    Triple(
                        "अग्निमीळे पुरोहितं यज्ञस्य देवमृत्विजम् |\nहोतारं रत्नधातमम् ||",
                        "मैं यज्ञ के पुरोहित, प्रकाशमान देव, ऋतु के अनुसार यज्ञ कराने वाले, देवताओं का आह्वान करने वाले और रत्नों को धारण करने वाले अग्निदेव की स्तुति करता हूँ।",
                        "हिन्दी भावार्थ: यह ऋग्वेद का सर्वप्रथम मंत्र है, जिसमें कल्याणकारी दिव्य प्रकाश (ज्ञान) की वंदना की गई है जो समस्त समृद्धि और तेज का मूल है।\n\nEnglish Explanation: This is the opening verse of Rigveda, praising the divine Agni, the priest and bestower of spiritual gems."
                    ),
                    Triple(
                        "द्यौः शान्तिरन्तरिक्षं शान्तिः पृथिवी शान्तिरापः शान्तिरोषधयः शान्तिः ||",
                        "द्युलोक में शांति हो, अंतरिक्ष में शांति हो, पृथ्वी पर शांति हो, जल में शांति हो, वनस्पति और औषधियां शांत और कल्याणकारी हों।",
                        "हिन्दी भावार्थ: यजुर्वेद का यह शांति पाठ संपूर्ण ब्रह्मांड, प्रकृति और मानव जाति के बीच परम सामंजस्य और सद्भाव की कामना करता है।\n\nEnglish Explanation: A Vedic prayer invoking supreme peace and harmony throughout the universe, nature, and all living beings."
                    ),
                    Triple(
                        "तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि धियो यो नः प्रचोदयात ||",
                        "हम सृष्टि के निर्माता उस दिव्य सविता देव के अति श्रेष्ठ तेज का ध्यान करते हैं, जो हमारी बुद्धियों को सत्कर्मों की ओर प्रेरित करे।",
                        "हिन्दी भावार्थ: यह गायत्री महामंत्र है, जो जीवन में सद्बुद्धि, आत्मिक प्रकाश, और ज्ञान की प्राप्ति की प्रार्थना करता है।\n\nEnglish Explanation: The sacred Gayatri Mantra, praying for intellect, divine wisdom, and spiritual illumination."
                    ),
                    Triple(
                        "सहृदयं सांमनस्यमविद्वेषं कृणोमि वः |\nअन्यो अन्यमभि हर्यत वत्सं जातमिव आघ्न्या ||",
                        "मैं आप सभी के हृदयों को समान विचार वाला, द्वेषरहित और प्रेमपूर्ण बनाता हूँ। आप आपस में वैसा ही प्रेम करें जैसा गाय अपने बछड़े से करती है।",
                        "हिन्दी भावार्थ: अथर्ववेद का यह मंत्र पारिवारिक और सामाजिक एकता, आपसी प्रेम, भाईचारे और अहिंसा का दिव्य संदेश देता है।\n\nEnglish Explanation: Encourages humanity to live in loving cooperation, harmony, mutual respect, and pure affection."
                    ),
                    Triple(
                        "सं गच्छध्वं सं वदध्वं सं वो मनांसि जानताम् |\nदेवा भागं यथा पूर्वे संजानाना उपासते ||",
                        "तुम सब एक साथ चलो, एक साथ बोलो, तुम्हारे मन एक समान होकर ज्ञान प्राप्त करें। जैसे प्राचीन काल में ज्ञानी जन एकमत होकर अपना कर्तव्य पूर्ण करते थे।",
                        "हिन्दी भावार्थ: ऋग्वेद का यह संगठन सूक्त समाज में लोकतांत्रिक एकता, सामूहिक चिंतन, और विकास की सुंदर शिक्षा देता है।\n\nEnglish Explanation: Inspires collective action, unity of purpose, speech, and intellectual collaboration for the betterment of society."
                    )
                )

                for (i in raw.indices) {
                    val vNum = startVerse + i
                    verses.add(FallbackVerse(vNum.toString(), raw[i].first, raw[i].second, raw[i].third))
                }
                return buildMultiVerseResponse(bookTitle, chTitle, part, verses)
            }

            // 6. Upanishads
            cleanBook.contains("उपनिषद") || cleanBook.contains("upanished") || cleanBook.contains("upanishad") -> {
                val bookTitle = "पवित्र उपनिषद (Holy Upanishads)"
                val chTitle = "अध्याय $chNum"
                
                val raw = listOf(
                    Triple(
                        "ॐ ईशावास्यमिदं सर्वं यत्किञ्च जगत्यां जगत् |\nतेन त्यक्तेन भुञ्जीथा मा गृधः कस्यस्विद्धनम् ||",
                        "इस चराचर ब्रह्मांड में जो कुछ भी है, वह सब ईश्वर से व्याप्त है। अतः निष्काम भाव से त्यागपूर्वक इसका भोग करो, किसी अन्य के धन का लोभ मत करो।",
                        "हिन्दी भावार्थ: ईशोपनिषद का यह मंत्र जीवन में अपरिग्रह, संतोष और त्यागपूर्वक उपभोग का संदेश देता है।\n\nEnglish Explanation: All of this is pervaded by the Divine. Therefore, enjoy what is given with a spirit of sacrifice and detachment."
                    ),
                    Triple(
                        "उत्तिष्ठत जाग्रत प्राप्य वरान्निबोधत |\nक्षुरस्य धारा निशिता दुरत्यया दुर्गं पथस्तत्कवयो वदन्ति ||",
                        "उठो, जागो! श्रेष्ठ महापुरुषों की शरण में जाकर सत्य ज्ञान को प्राप्त करो। ज्ञानी जन इस ज्ञान मार्ग को छुरे की तीखी धार के समान दुर्गम बताते हैं।",
                        "हिन्दी भावार्थ: कठोपनिषद का यह मंत्र हमें आध्यात्मिक सजगता और गुरु कृपा से आत्मज्ञान के कठिन मार्ग पर बढ़ने की सीख देता है।\n\nEnglish Explanation: Arise! Awake! Approach the wise teachers and realize the Self. The path is sharp and difficult like a razor's edge."
                    ),
                    Triple(
                        "सत्यमेव जयते नानृतं सत्येन पन्था विततो देवयानः ||",
                        "सत्य की ही सदा विजय होती है, असत्य की नहीं। सत्य के द्वारा ही देवयान मार्ग (दिव्य कल्याण का पथ) प्रशस्त होता है।",
                        "हिन्दी भावार्थ: मुण्डकोपनिषद का यह सूत्र सत्य की शक्ति और उसकी विजय का शाश्वत प्रमाण है।\n\nEnglish Explanation: Truth alone triumphs, not untruth. By Truth is laid out the divine path of the gods."
                    ),
                    Triple(
                        "ॐ असतो मा सद्गमय | तमसो मा ज्योतिर्गमय | मृत्योर्मा अमृतं गमय ||",
                        "हमें असत्य से सत्य की ओर ले चलो, अंधकार (अज्ञान) से प्रकाश (ज्ञान) की ओर ले चलो, और मृत्यु से अमरता की ओर ले चलो।",
                        "हिन्दी भावार्थ: बृहदारण्यक उपनिषद की यह पावन प्रार्थना मानव मात्र के आध्यात्मिक कल्याण और परम चेतना की ओर बढ़ने की कामना करती है।\n\nEnglish Explanation: Lead us from untruth to truth, from darkness to light, and from death to immortality."
                    ),
                    Triple(
                        "अहं ब्रह्मास्मि ||",
                        "मैं ही ब्रह्म हूँ (मेरी आत्मा परम चेतना का ही अंश है)।",
                        "हिन्दी भावार्थ: यह बृहदारण्यक उपनिषद का महावाक्य है, जो जीव और ब्रह्म (परमेश्वर) की एकता को दर्शाता है।\n\nEnglish Explanation: \"I am the ultimate reality.\" It proclaims the spiritual oneness of the individual soul with the cosmic consciousness."
                    )
                )

                for (i in raw.indices) {
                    val vNum = startVerse + i
                    verses.add(FallbackVerse(vNum.toString(), raw[i].first, raw[i].second, raw[i].third))
                }
                return buildMultiVerseResponse(bookTitle, chTitle, part, verses)
            }

            // 7. Dhammapada & Buddhist
            cleanBook.contains("धम्मपद") || cleanBook.contains("dhammapada") || cleanBook.contains("त्रिपिटक") || cleanBook.contains("tripitaka") || cleanBook.contains("बुद्ध") -> {
                val bookTitle = "धम्मपद (Dhammapada)"
                val chTitle = "वर्ग $chNum"
                
                val raw = listOf(
                    Triple(
                        "मनोपुब्बङ्गमा धम्मा, मनोसिट्ठा मनोमया |\nमनसा चे पदुट्ठेन, भासति वा करोति वा, ततो नं दुक्खमन्वेति चक्कं व वहतो पदं ||",
                        "सभी विचार मन से उत्पन्न होते हैं, मन ही उनका स्वामी है। यदि कोई दूषित मन से बोलता या कर्म करता है, तो दुःख वैसे ही उसका पीछा करता है जैसे गाड़ी का पहिया बैल के पीछे चलता है।",
                        "हिन्दी भावार्थ: भगवान बुद्ध कर्म सिद्धांत को स्पष्ट करते हुए बताते हैं कि हमारे सारे दुखों का मूल कारण हमारे दूषित विचार और मन की अपवित्रता ही है।\n\nEnglish Explanation: Mind precedes all mental states. If one speaks or acts with an impure mind, suffering follows even as the wheel follows the ox."
                    ),
                    Triple(
                        "मनोपुब्बङ्गमा धम्मा, मनोसिट्ठा मनोमया |\nमनसा चे पसन्नेन, भासति वा करोति वा, ततो नं सुखमन्वेति छाया व अनपायिनी ||",
                        "सभी विचार मन से उत्पन्न होते हैं, मन ही उनका स्वामी है। यदि कोई शुद्ध व प्रसन्न मन से बोलता या कर्म करता है, तो सुख उसकी छाया की तरह सदा उसके पीछे चलता है।",
                        "हिन्दी भावार्थ: शुद्ध विचारों और सकारात्मक दृष्टिकोण से किया गया कर्म जीवन में शाश्वत सुख, शांति और संतोष की वर्षा करता है।\n\nEnglish Explanation: Mind precedes all mental states. If one speaks or acts with a pure mind, happiness follows like a shadow, never leaving."
                    ),
                    Triple(
                        "अप्पमादो अमतपदं, पमादो मच्चु नो पदं |\nअप्पमत्ता न मिय्यन्ति, ye पमत्ता यथा मता ||",
                        "सजयता (अप्रमाद) अमृत का मार्ग है, और आलस्य (प्रमाद) मृत्यु का मार्ग है। जो सजग हैं वे कभी नष्ट नहीं होते, जो आलसी हैं वे पहले से ही मृत समान हैं।",
                        "हिन्दी भावार्थ: जीवन में जागरूकता, सजगता और ध्यान ही सत्य (अमृत) की ओर ले जाते हैं, जबकि आलस्य पतन का कारण है।\n\nEnglish Explanation: Mindfulness is the path to immortality; negligence is the path to death. The mindful do not die, the negligent are as if already dead."
                    ),
                    Triple(
                        "न हि वेरेन वेरानि, सम्मन्तीध कुदाचनं |\nअवेरेन च सम्मन्ति, एस धम्मो सनन्तनो ||",
                        "इस संसार में कभी भी शत्रुता से शत्रुता शांत नहीं होती। शत्रुता केवल मित्रता और प्रेम (अवेर) से ही शांत होती है; यही सनातन नियम है।",
                        "हिन्दी भावार्थ: हिंसा और नफरत का जवाब नफरत नहीं हो सकता। क्षमा, करुणा और प्रेम ही समाज में स्थाई शांति स्थापित कर सकते हैं।\n\nEnglish Explanation: Hatred is never appeased by hatred in this world. By non-hatred alone is hatred appeased. This is a law eternal."
                    ),
                    Triple(
                        "आरोग्यपरमा लाभा सन्तुट्ठीपरमं धनं |\nविस्सासपरमा ञाती निब्बानं परमं सुखं ||",
                        "स्वास्थ्य सबसे बड़ा लाभ है, संतोष सबसे बड़ा धन है, विश्वास सबसे बड़ा संबंधी है, और निर्वाण परम सुख है।",
                        "हिन्दी भावार्थ: शारीरिक व मानसिक स्वास्थ्य, जीवन में संतुष्टि का भाव, और आपसी विश्वास ही जीवन की वास्तविक पूंजी हैं।\n\nEnglish Explanation: Health is the highest gain, contentment the greatest wealth, trust the best relation, and Nirvana the highest happiness."
                    )
                )

                for (i in raw.indices) {
                    val vNum = startVerse + i
                    verses.add(FallbackVerse(vNum.toString(), raw[i].first, raw[i].second, raw[i].third))
                }
                return buildMultiVerseResponse(bookTitle, chTitle, part, verses)
            }

            // 8. Guru Granth Sahib & Gurbani
            cleanBook.contains("ग्रंथ") || cleanBook.contains("granth") || cleanBook.contains("गुरु") || cleanBook.contains("gurbani") -> {
                val bookTitle = "श्री गुरु ग्रंथ साहिब (Sri Guru Granth Sahib)"
                val chTitle = "अंग/पौड़ी $chNum"
                
                val raw = listOf(
                    Triple(
                        "ੴ ਸਤਿ ਨਾਮੁ ਕਰਤਾ ਪੁਰਖੁ ਨਿਰਭਉ ਨਿਰਵੈਰੁ ਅਕਾਲ ਮੂਰਤਿ ਅਜੂਨੀ ਸੈਭੰ ਗੁਰ ਪ੍ਰਸਾਦਿ ॥",
                        "एक अकाल पुरख (ईश्वर) है, जिसका नाम सत्य है, जो सृष्टि का रचयिता है, भय से रहित है, वैर-भाव से मुक्त है, समय से परे है, जन्म-मरण के बंधन से मुक्त है, स्वयंभू है और गुरु की कृपा से प्राप्त होता है।",
                        "हिन्दी भावार्थ: यह सिखों का मूल मंत्र है, जो ईश्वर के निर्गुण और शाश्वत स्वरूप को अद्भुत रूप से प्रकट करता है।\n\nEnglish Explanation: One Universal Creator God, The Supreme Truth, The Creator, Without Fear, Without Hate, Timeless, Unborn, Self-Existent, realized by the Guru's Grace."
                    ),
                    Triple(
                        "ਆਦਿ ਸਚੁ ਜੁਗਾਦਿ ਸਚੁ ॥ ਹੈ ਭੀ ਸਚੁ ਨਾਨਕ ਹੋਸੀ ਭੀ ਸਚੁ ॥੧॥",
                        "वह परमेश्वर आदि काल से सत्य है, युगों के प्रारंभ में भी सत्य था, वर्तमान में भी वही सत्य है, और हे नानक! भविष्य में भी वही सत्य रहेगा।",
                        "हिन्दी भावार्थ: संसार की हर वस्तु परिवर्तनशील और नश्वर है, केवल एक परमात्मा ही शाश्वत सत्य है।\n\nEnglish Explanation: \"True in the beginning, True through the ages, True even now, O Nanak, He shall forever be True.\""
                    ),
                    Triple(
                        "ਹੁਕਮੀ ਹੋਵਨਿ ਆਕਾਰ ਹੁਕਮੁ ਨ ਕਹਿਆ ਜਾਈ ॥\nਹੁਕਮੀ ਹੋਵਨਿ ਜੀਅ ਹੁਕਮਿ ਮਿਲੈ ਵਡਿਆਈ ॥",
                        "परमात्मा के हुक्म (ईश्वरीय नियम) से ही सभी आकारों और शरीरों का सृजन होता है, उस हुक्म को शब्दों में व्यक्त नहीं किया जा सकता। ईश्वरीय नियम से ही सभी जीवों को जीवन और महिमा मिलती है।",
                        "हिन्दी भावार्थ: सब कुछ परमात्मा की इच्छा और सार्वभौमिक नियम के अंतर्गत ही संचालित होता है।\n\nEnglish Explanation: By His Command, bodies are created; His Command cannot be described. By His Command, souls come into being; by His Command, greatness is obtained."
                    ),
                    Triple(
                        "ਮਨੁ ਹਾਲੀ ਕਿਰਸਾਣੀ ਕਰਣੀ ਸਰਮੁ ਪਾਣੀ ਤਨੁ ਖੇਤੁ ॥\nਨਾਮੁ ਬੀਜੁ ਸੰਤੋਖੁ ਸੁਹਾਗਾ ਰਖੁ ਗਰੀਬੀ ਵੇਸੁ ॥",
                        "अपने मन को हलवाहा बनाओ, श्रेष्ठ कर्मों को खेती बनाओ, संकोच और विनम्रता रूपी जल से उसे सींचो, और इस शरीर को खेत बनाओ। नाम-सिमरन रूपी बीज डालो, संतोष रूपी सुहागा फेरो, और विनम्रता का वेश धारण करो।",
                        "हिन्दी भावार्थ: यह आध्यात्मिक खेती का सुंदर रूपक है, जो जीवन में सद्गुणों के बीज बोने और भक्ति की खेती करने की शिक्षा देता है।\n\nEnglish Explanation: Make your mind the farmer, good deeds the farming, modesty the water, and your body the field. Let the Lord's Name be the seed, contentment the furrow, and humility the garment."
                    ),
                    Triple(
                        "ਕਰਮੀ ਆਵੈ ਕਪੜਾ ਨਦਰੀ ਮੋਖੁ ਦੁਆਰੁ ॥\nਨਾਨਕ ਏਵੈ ਜਾਣੀਐ ਸਭੁ ਆਪੇ ਸਚਿਆਰੁ ॥",
                        "हमारे अच्छे और बुरे कर्मों के अनुसार ही हमें यह मानव शरीर रूपी वस्त्र प्राप्त होता है, परंतु मोक्ष का द्वार तो केवल परमात्मा की दया दृष्टि (नदर) से ही मिलता है।",
                        "हिन्दी भावार्थ: कर्म हमें जीवन प्रदान करते हैं, परंतु अंतिम मुक्ति और कल्याण केवल ईश्वर की कृपा से ही संभव है।\n\nEnglish Explanation: By the karma of past actions, the robe of this physical body is obtained. But by His Grace, the gate of liberation is found."
                    )
                )

                for (i in raw.indices) {
                    val vNum = startVerse + i
                    verses.add(FallbackVerse(vNum.toString(), raw[i].first, raw[i].second, raw[i].third))
                }
                return buildMultiVerseResponse(bookTitle, chTitle, part, verses)
            }

            // 9. Generic Fallback for ANY other book (Mahabharata, Puranas, Jain Agamas, Talmud, etc.!)
            else -> {
                val raw = getGenericFallbackVerses(bookName, startVerse)
                for (i in raw.indices) {
                    val vNum = startVerse + i
                    verses.add(FallbackVerse(vNum.toString(), raw[i].original, raw[i].translation, raw[i].explanation))
                }
                return buildMultiVerseResponse(bookName, "अध्याय $chNum", part, verses)
            }
        }
    }

    private fun getGenericFallbackVerses(bookName: String, startVerse: Int): List<FallbackVerse> {
        val cleanBook = bookName.lowercase()
        val list = mutableListOf<FallbackVerse>()
        
        val raw = when {
            cleanBook.contains("महाभारत") || cleanBook.contains("mahabharat") -> listOf(
                Triple("यतो धर्मस्ततो जयः।", "जहाँ धर्म (सत्य और कर्तव्य) है, वहाँ सदैव विजय होती है।", "हिन्दी भावार्थ: यह महाभारत का मूल संदेश है जो सिखाता है कि सत्य और सदाचार की हमेशा अंतिम विजय होती है।\n\nEnglish: Where there is righteousness, there is victory."),
                Triple("अहिंसा परमो धर्मः।", "अहिंसा ही परम धर्म और कर्तव्य है।", "हिन्दी भावार्थ: महाभारत के अनुसार किसी भी जीव को मन, वचन और कर्म से पीड़ा न देना ही सबसे बड़ा धर्म है।\n\nEnglish: Non-violence is the supreme virtue."),
                Triple("धर्म एव हतो हन्ति धर्मो रक्षति रक्षितः।", "नष्ट किया हुआ धर्म मारने वाले को मार डालता है, और रक्षा किया हुआ धर्म रक्षा करने वाले की रक्षा करता है।", "हिन्दी भावार्थ: यदि हम धर्म और नैतिक मूल्यों की रक्षा करते हैं, तो वे मूल्य जीवन के संकटों में हमारी रक्षा करते हैं।\n\nEnglish: Righteousness protects those who protect it, and destroys those who destroy it."),
                Triple("सत्यं वद धर्मं चर।", "सत्य बोलो, धर्म का आचरण करो।", "हिन्दी भावार्थ: जीवन के कल्याण के लिए सत्य बोलना और सदाचार का पालन करना सर्वोपरि है।\n\nEnglish: Speak the truth, walk the path of righteousness."),
                Triple("उदारचरितानां तु वसुधैव कुटुम्बकम्।", "उदार हृदय वालों के लिए संपूर्ण पृथ्वी ही उनका परिवार है।", "हिन्दी भावार्थ: संकुचित बुद्धि वाले लोग 'यह मेरा है, यह पराया है' ऐसा सोचते हैं, जबकि विशाल हृदय वाले पूरे विश्व को अपना घर मानते हैं।\n\nEnglish: For the broad-minded, the entire world is one family.")
            )
            cleanBook.contains("जैन") || cleanBook.contains("आगम") || cleanBook.contains("समयसार") || 
            cleanBook.contains("तत्त्वार्थ") || cleanBook.contains("द्रव्यसंग्रह") || cleanBook.contains("श्रावकाचार") -> listOf(
                Triple("अहिंसा परमो धर्मः।", "अहिंसा ही परम धर्म है। सभी जीवों के प्रति दया रखें।", "हिन्दी भावार्थ: यह जैन दर्शन का मूल आधार स्तंभ है, जो समस्त प्राणियों के प्रति अहिंसा और करुणा का उपदेश देता है।\n\nEnglish: Non-violence is the supreme religion."),
                Triple("परस्परोपग्रहो जीवाणाम्।", "सभी जीव परस्पर एक-दूसरे की सहायता और उपकार करने के लिए हैं।", "हिन्दी भावार्थ: तत्त्वार्थ सूत्र का यह सूत्र समस्त जीवन की अंतर्निर्भरता और परस्पर सहयोग को दर्शाता है।\n\nEnglish: All living beings are meant to assist and support one another."),
                Triple("सम्यग्दर्शनज्ञानचारित्राणि मोक्षमार्गः।", "सम्यक् श्रद्धा, सम्यक् ज्ञान और सम्यक् आचरण ही मोक्ष का सच्चा मार्ग है।", "हिन्दी भावार्थ: जैन दर्शन के रत्नत्रय सिद्धांत का निरूपण, जो जीवन की पूर्णता और मोक्ष की प्राप्ति का मार्ग प्रदर्शित करता है।\n\nEnglish: Right faith, right knowledge, and right conduct together constitute the path to liberation."),
                Triple("आदा कत्ता स हि कम्मं।", "आत्मा स्वयं ही अपने कर्मों का कर्ता है और स्वयं ही उसका फल भोगता है।", "हिन्दी भावार्थ: मनुष्य अपने भाग्य और कर्मों का निर्माता स्वयं है, किसी अन्य बाह्य शक्ति का इसमें हस्तक्षेप नहीं है।\n\nEnglish: The soul itself is the doer of its actions and the enjoyer of their fruits."),
                Triple("अप्पप्पा सुद्धो सिधो।", "आत्मा स्वयं में शुद्ध, बुद्ध और सिद्ध स्वरूप है।", "हिन्दी भावार्थ: निज आत्मा के शुद्ध चैतन्य और अनंत ज्ञान स्वरूप का बोध कराने वाला उपदेश।\n\nEnglish: The soul in its pure state is inherently clean, enlightened, and free.")
            )
            cleanBook.contains("तनाख") || cleanBook.contains("tanakh") || cleanBook.contains("तोराह") || 
            cleanBook.contains("तल्मूड") || cleanBook.contains("talmud") || cleanBook.contains("ज़ोहर") || cleanBook.contains("zohar") -> listOf(
                Triple("שְׁמַע יִשְׂרָאֵל יְהוָה אֱלֹהֵינוּ יְהוָה אֶחָד׃ (Sh'ma Yisrael)", "सुनो ओ इसराइल, प्रभु हमारा परमेश्वर, प्रभु एक है।", "हिन्दी भावार्थ: यह यहूदी धर्म का केंद्रीय और अत्यंत पवित्र विश्वास मंत्र है।\n\nEnglish: Hear, O Israel: The Lord our God, the Lord is One."),
                Triple("Love your neighbor as yourself.", "अपने पड़ोसी से अपने ही समान प्रेम करो।", "हिन्दी भावार्थ: यह नैतिक आचरण और मानवता की सेवा का परम नियम है।\n\nEnglish: Love your neighbor as yourself (Leviticus 19:18)."),
                Triple("The world stands on three things: on Torah, on service, and on acts of loving-kindness.", "यह संसार तीन स्तंभों पर टिका है: ज्ञान (तोराह), सेवा, और दयालुता के कार्य।", "हिन्दी भावार्थ: तल्मूड की सुंदर दार्शनिक सूक्ति जो जीवन और समाज के संतुलन को स्पष्ट करती है।\n\nEnglish: The world stands on three things: on study, on service, and on acts of loving-kindness."),
                Triple("Create in me a pure heart, O God, and renew a steadfast spirit within me.", "हे परमेश्वर, मेरे अंदर एक शुद्ध हृदय उत्पन्न कर, और मेरे भीतर एक स्थिर आत्मा को जागृत कर।", "हिन्दी भावार्थ: हृदय की पवित्रता और मानसिक स्थिरता के लिए प्रार्थना।\n\nEnglish: Create in me a clean heart, O God, and renew a right spirit within me (Psalm 51:10)."),
                Triple("The light of the soul is the candle of the Lord, searching all the inmost parts.", "मनुष्य की आत्मा प्रभु का दीपक है, जो अंतर्मन के गहरे रहस्यों को प्रकाशित करती है।", "हिन्दी भावार्थ: आंतरिक चेतना और दैवीय प्रकाश की महिमा।\n\nEnglish: The spirit of man is the lamp of the Lord, searching all the innermost parts of his being (Proverbs 20:27).")
            )
            else -> listOf(
                Triple("सर्वं परवशं दुःखं सर्वमात्मवशं सुखम्।", "जो दूसरों के अधीन है वह दुःख है, जो अपने अधीन (आत्मनिर्भर) है वह सुख है।", "हिन्दी भावार्थ: स्वावलंबन और आत्म-नियंत्रण ही सुख का एकमात्र स्रोत हैं।\n\nEnglish: Dependence on others is misery; self-reliance is happiness."),
                Triple("उदारचरितानां तु वसुधैव कुटुम्बकम्।", "उदार हृदय वालों के लिए संपूर्ण पृथ्वी ही एक परिवार है।", "हिन्दी भावार्थ: वैश्विक परिवार और वसुधैव कुटुंबकम् की पवित्र भावना।\n\nEnglish: For the broad-minded, the entire world is one family."),
                Triple("सत्यमेव जयते नानृतं सत्येन पन्था विततो देवयानः।", "सत्य की ही सदा विजय होती है, असत्य की नहीं।", "हिन्दी भावार्थ: सत्य और सदाचार के कल्याणकारी मार्ग की महिमा।\n\nEnglish: Truth alone triumphs, not falsehood; by truth is laid out the path to the divine."),
                Triple("सर्वे भवन्तु सुखिनः सर्वे सन्तु निरामयाः।", "सभी सुखी रहें, सभी रोगमुक्त और स्वस्थ रहें।", "हिन्दी भावार्थ: सनातन संस्कृति का संपूर्ण विश्व के लिए कल्याणकारी प्रार्थना मंत्र।\n\nEnglish: May all beings be happy; may all beings be free from illness."),
                Triple("तमसो मा ज्योतिर्गमय। मृत्योर्मामृतं गमय।", "हमें अंधकार से प्रकाश की ओर और मृत्यु से अमरता की ओर ले चलो।", "हिन्दी भावार्थ: समस्त जीवों के कल्याण और आत्मज्ञान की पावन कामना।\n\nEnglish: Lead me from darkness to light, and from death to immortality.")
            )
        }

        for (i in raw.indices) {
            val vNum = startVerse + i
            list.add(FallbackVerse(vNum.toString(), raw[i].first, raw[i].second, raw[i].third))
        }
        return list
    }

    private fun buildMultiVerseResponse(
        book: String,
        chapter: String,
        part: String,
        verses: List<FallbackVerse>
    ): String {
        return verses.joinToString("\n===\n") { verse ->
            """
                VERSE_NO: ${verse.verseNo}
                ORIGINAL: [ $book - $chapter ]
                ${verse.original}
                TRANSLATION: ${verse.translation}
                EXPLANATION: ${verse.explanation}
            """.trimIndent()
        }
    }

    // PDF Link list for downloading/reading full books in online reader
    fun getFullBookPdfUrl(bookName: String): String? {
        val cleanBook = bookName.lowercase()
        return try {
            when {
                cleanBook.contains("गीता") || cleanBook.contains("gita") -> "https://archive.org/details/srimad-bhagavad-gita-gita-press-gorakhpur"
                cleanBook.contains("कुरान") || cleanBook.contains("quran") -> "https://archive.org/details/the-holy-quran-hindi-translation-and-commentary"
                cleanBook.contains("बाइबल") || cleanBook.contains("bible") -> "https://archive.org/details/hindi-bible-holy-scriptures-easy-to-read-version-erv-2005"
                cleanBook.contains("रामचरितमानस") || cleanBook.contains("ramcharitmanas") -> "https://archive.org/details/shri-ram-charit-manas-gita-press"
                cleanBook.contains("रामायण") || cleanBook.contains("ramayana") -> "https://archive.org/search.php?query=Valmiki+Ramayana+Gita+Press+Hindi+pdf"
                cleanBook.contains("महाभारत") || cleanBook.contains("mahabharat") -> "https://archive.org/search.php?query=Mahabharat+Gita+Press+Hindi+pdf"
                cleanBook.contains("वेद") || cleanBook.contains("veda") -> "https://archive.org/search.php?query=Rigveda+Samhita+Hindi+translation+pdf"
                cleanBook.contains("उपनिषद") || cleanBook.contains("upanished") || cleanBook.contains("upanishad") -> "https://archive.org/search.php?query=108+Upanishads+Gita+Press+Hindi+pdf"
                cleanBook.contains("धम्मपद") || cleanBook.contains("dhammapada") -> "https://archive.org/search.php?query=Dhammapada+Hindi+translation+pdf"
                cleanBook.contains("ग्रंथ") || cleanBook.contains("granth") -> "https://archive.org/search.php?query=Sri+Guru+Granth+Sahib+Hindi+translation+pdf"
                cleanBook.contains("सुश्रुत") || cleanBook.contains("sushruta") -> "https://archive.org/search.php?query=Sushruta+Samhita+Hindi+translation+pdf"
                cleanBook.contains("पुराण") || cleanBook.contains("purana") -> "https://archive.org/search.php?query=Puranas+Gita+Press+Hindi+pdf"
                cleanBook.contains("ब्रह्मसूत्र") || cleanBook.contains("brahmasutra") -> "https://archive.org/search.php?query=Brahmasutra+Gita+Press+Hindi+pdf"
                cleanBook.contains("त्रिपिटक") || cleanBook.contains("tripitaka") -> "https://archive.org/search.php?query=Tripitaka+Hindi+translation+pdf"
                cleanBook.contains("आगम") || cleanBook.contains("jain") -> "https://archive.org/search.php?query=Jain+Agams+Hindi+pdf"
                cleanBook.contains("समयसार") || cleanBook.contains("samayasara") -> "https://archive.org/search.php?query=Samayasara+Kundakunda+Hindi+pdf"
                cleanBook.contains("तत्त्वार्थ") || cleanBook.contains("tattvartha") -> "https://archive.org/search.php?query=Tattvartha+Sutra+Hindi+pdf"
                cleanBook.contains("तनाख") || cleanBook.contains("tanakh") -> "https://archive.org/search.php?query=Tanakh+English+pdf"
                cleanBook.contains("तल्मूड") || cleanBook.contains("talmud") -> "https://archive.org/search.php?query=Talmud+Babylonian+English+pdf"
                cleanBook.contains("ज़ोहर") || cleanBook.contains("zohar") -> "https://archive.org/search.php?query=Zohar+English+pdf"
                else -> "https://archive.org/search.php?query=" + URLEncoder.encode(bookName + " pdf book", "UTF-8")
            }
        } catch (e: Exception) {
            "https://archive.org"
        }
    }

    private val bgChapter1Part1 = """
        VERSE_NO: 1
        ORIGINAL: धृतराष्ट्र उवाच |
        धर्मक्षेत्रे कुरुक्षेत्रे समवेता युयुत्सवः |
        मामकाः पाण्डवाश्चैव किमकुर्वत सञ्जय || १ ||
        TRANSLATION: धृतराष्ट्र बोले— हे संजय! धर्मभूमि कुरुक्षेत्र में एकत्रित, युद्ध की इच्छा वाले मेरे और पाण्डु के पुत्रों ने क्या किया?
        EXPLANATION: हिन्दी भावार्थ: कुरुक्षेत्र को केवल भौतिक युद्धक्षेत्र नहीं वरन धर्मक्षेत्र भी कहा गया है। यह श्लोक मनुष्य के अंतर्मन में चलने वाले अच्छे और बुरे विचारों के संघर्ष (धर्म बनाम अधर्म) का प्रतीक है।
        English Explanation: Dhritarashtra asks Sanjaya about what his sons and the sons of Pandu did when they gathered at the holy land of Kurukshetra, eager for battle. Symbolically, Kurukshetra represents the inner battlefield of the human mind where divine and demonic tendencies constantly clash.
        ===
        VERSE_NO: 2
        ORIGINAL: सञ्जय उवाच |
        दृष्ट्वा तु पाण्डवानीकं व्यूढं दुर्योधनस्तदा |
        आचार्यमुपसङ्गम्य राजा वचनमब्रवीत् || २ ||
        TRANSLATION: संजय बोले— उस समय राजा दुर्योधन ने व्यूह रचना युक्त पाण्डवों की सेना को देखकर द्रोणाचार्य के पास जाकर यह वचन कहा।
        EXPLANATION: हिन्दी भावार्थ: दुर्योधन अपनी घबराहट और असुरक्षा की भावना को छिपाने के लिए अपने गुरु द्रोणाचार्य के पास जाता है।
        English Explanation: Sanjaya replies that King Duryodhana, upon seeing the Pandava army strategically arranged in battle formation, approached his teacher Drona to speak to him.
        ===
        VERSE_NO: 3
        ORIGINAL: पश्यैतां पाण्डुपुत्राणामाचार्य महतीं चमूम् |
        व्यूढां द्रुपदपुत्रेण तव शिष्येण धीमता || ३ ||
        TRANSLATION: हे आचार्य! आपके बुद्धिमान शिष्य द्रुपदपुत्र (धृष्टद्युम्न) द्वारा व्यूहाकार खड़ी की हुई पाण्डुपुत्रों की इस बड़ी भारी सेना को देखिए।
        EXPLANATION: हिन्दी भावार्थ: दुर्योधन द्रोणाचार्य को उनके पूर्व शिष्यों की शक्ति दिखाकर सतर्क और प्रेरित करना चाहता है।
        English Explanation: Duryodhana points out to his teacher Drona the massive army of the Pandavas assembled by Drona's own talented disciple, the son of Drupada (Dhrishtadyumna).
        ===
        VERSE_NO: 4
        ORIGINAL: अत्र शूरा महेष्वासा भीमार्जुनसमा युधि |
        युयुधानो विराटश्च द्रुपदश्च महारथः || ४ ||
        TRANSLATION: इस सेना में भीम और अर्जुन के समान युद्ध करने वाले अनेक वीर धनुर्धर हैं, जैसे सात्यकि, विराट और महारथी द्रुपद।
        EXPLANATION: हिन्दी भावार्थ: पाण्डव सेना के अग्रगण्य योद्धाओं का वर्णन कर दुर्योधन अपनी युद्ध चिंताओं को व्यक्त करता है।
        English Explanation: Duryodhana lists the mighty warriors in the Pandava army who are equal in military prowess to Bhima and Arjuna, such as Yuyudhana, Virata, and the great chariot warrior Drupada.
    """.trimIndent()

    private val bgChapter1Part2 = """
        VERSE_NO: 16
        ORIGINAL: अनन्तविजयं राजा कुन्तीपुत्रो युधिष्ठिरः |
        नकुलः सहदेवश्च सुघोषमणिपुष्पकौ || १६ ||
        TRANSLATION: कुन्तीपुत्र राजा युधिष्ठिर ने अनन्तविजय नामक, और नकुल तथा सहदेव ने सुघोष और मणिपुष्पक नामक शंख बजाए।
        EXPLANATION: हिन्दी भावार्थ: प्रत्येक पाण्डव भ्राता ने अपने दिव्य शंखों को फूंककर अधर्म के विरुद्ध धर्म की शंखनाद की।
        English Explanation: King Yudhishthira, the son of Kunti, blew his conch named Anantavijaya, while Nakula and Sahadeva blew Sughosha and Manipushpaka.
        ===
        VERSE_NO: 20
        ORIGINAL: अथ व्यवस्थितान्दृष्ट्वा धार्तराष्ट्रान् कपिध्वजः |
        प्रवृत्ते शस्त्रसम्पाते धनुरुद्यम्य पाण्डवः |
        हृषीकेशं तदा वाक्यमिदमाह महीपते || २० ||
        TRANSLATION: हे राजन्! इसके बाद शस्त्र चलने की तैयारी के समय हनुमानजी के चिह्न वाली ध्वजा से युक्त पाण्डुपुत्र अर्जुन ने धृतराष्ट्र के पुत्रों को व्यवस्थित देखकर धनुष उठाकर हृषीकेश श्रीकृष्ण से यह वचन कहा।
        EXPLANATION: हिन्दी भावार्थ: अर्जुन युद्ध की विभीषिका और अपनों के विनाश की सोच से व्याकुल होने ही वाला है। हनुमान जी का उसकी ध्वजा पर होना विजय का दिव्य सूचक है।
        English Explanation: Seeing the sons of Dhritarashtra arrayed before him, Arjuna, whose chariot banner bore the emblem of Hanuman, raised his bow and addressed Lord Krishna (Hrishikesha) as the arrows were about to fly.
        ===
        VERSE_NO: 21
        ORIGINAL: अर्जुन उवाच |
        सेनयोरुभयोर्मध्ये रथं स्थापय मेऽच्युत |
        यावदेतान्निरीक्षेऽहं योद्धुकामानवस्थितान् || २१ ||
        TRANSLATION: अर्जुन बोले— हे अच्युत! मेरे रथ को दोनों सेनाओं के बीच में खड़ा कीजिए, जिससे मैं इन युद्ध की इच्छा रखने वालों को देख सकूँ।
        EXPLANATION: हिन्दी भावार्थ: अर्जुन स्वयं देखना चाहते हैं कि अधर्म का पक्ष लेने के लिए कुरुक्षेत्र में कौन-कौन उपस्थित हैं।
        English Explanation: Arjuna requests Lord Krishna to position his chariot between the two opposing armies so that he may inspect those who have assembled to fight on the side of evil.
    """.trimIndent()

    private val bgChapter2Part1 = """
        VERSE_NO: 1
        ORIGINAL: सञ्जय उवाच |
        तं तथा कृपयाविष्टमश्रुपूर्णाकुलेक्षणम् |
        विषीदन्तमिदं वाक्यमुवाच मधुसूदनः || १ ||
        TRANSLATION: संजय बोले— उस प्रकार करुणा से व्याप्त, आँसुओं से भरे हुए व्याकुल नेत्रों वाले और शोकयुक्त अर्जुन से मधुसूदन भगवान श्रीकृष्ण ने यह वचन कहा।
        EXPLANATION: हिन्दी भावार्थ: यहाँ से श्रीमद्भगवद्गीता के दिव्य ज्ञान की शुरुआत होती है, जहाँ श्री कृष्ण व्याकुल अर्जुन को जीवन का शाश्वत सत्य समझाते हैं।
        English Explanation: Sanjaya says that seeing Arjuna overwhelmed with pity, despondent, and his eyes filled with tears, Lord Krishna (Madhusudana) spoke these words.
        ===
        VERSE_NO: 2
        ORIGINAL: श्रीभगवानुवाच |
        कुतस्त्वा कश्मलमिदं विषमे समुपस्थितम् |
        अनार्यजुष्टमस्वर्ग्यमकीर्तिकरमर्जुन || २ ||
        TRANSLATION: श्रीभगवान बोले— हे अर्जुन! इस विषम परिस्थिति में तुम्हें यह अज्ञान (मोह) कहाँ से प्राप्त हुआ? यह श्रेष्ठ पुरुषों के योग्य नहीं है, न स्वर्ग देने वाला है और न ही कीर्ति दिलाने वाला है।
        EXPLANATION: हिन्दी भावार्थ: भगवान अर्जुन को याद दिलाते हैं कि कर्तव्य से भागना कायरता है, और एक सच्चे कर्मयोगी के लक्षण इसके सर्वथा विपरीत होते हैं।
        English Explanation: The Supreme Lord asks Arjuna from where this delusion and weakness of heart has come upon him in this critical hour. He warns that it is unbefitting an honorable person, leads to infamy, and prevents spiritual progress.
        ===
        VERSE_NO: 3
        ORIGINAL: क्लैब्यं मा स्म गमः पार्थ नैतत्त्वय्युपपद्यते |
        क्षुद्रं हृदयदौर्बल्यं त्यक्त्वोत्तिष्ठ परन्तप || ३ ||
        TRANSLATION: हे पार्थ! नपुंसकता (कायरता) को मत प्राप्त होओ, यह तुम्हारे योग्य नहीं है। हे परंतप! हृदय की इस तुच्छ दुर्बलता को त्यागकर युद्ध के लिए खड़े हो जाओ।
        EXPLANATION: हिन्दी भावार्थ: मन की कमज़ोरी ही हमारी सबसे बड़ी शत्रु है। श्रीकृष्ण अर्जुन को मानसिक रूप से सुदृढ़ होने का दिव्य संदेश देते हैं।
        English Explanation: Lord Krishna urges Arjuna not to yield to impotence and cowardice, for it does not become him. He commands him to cast off this petty weakness of heart and stand up for righteousness.
    """.trimIndent()

    private val bgChapter2Part2 = """
        VERSE_NO: 47
        ORIGINAL: कर्मण्येवाधिकारस्ते मा फलेषु कदाचन |
        मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि || ४७ ||
        TRANSLATION: तुम्हारा अधिकार केवल कर्म करने में ही है, उसके फलों में कभी नहीं। तुम कर्मों के फल का हेतु मत बनो और तुम्हारी कर्म न करने में भी आसक्ति न हो।
        EXPLANATION: हिन्दी भावार्थ: यह गीता का सबसे प्रसिद्ध और केंद्रीय श्लोक है। निष्काम कर्मयोग का यह सिद्धांत सिखाता है कि हमें फल की चिंता किए बिना केवल अपने कर्तव्य पथ पर केंद्रित रहना चाहिए।
        English Explanation: You have a right to perform your prescribed duty, but you are not entitled to the fruits of your actions. Never consider yourself the cause of the results of your activities, and never be attached to inaction.
        ===
        VERSE_NO: 50
        ORIGINAL: बुद्धियुक्तो जहातीह उभे सुकृतदुष्कृते |
        तस्माद्योगाय युज्यस्व योगः कर्मसु कौशलम् || ५० ||
        TRANSLATION: समबुद्धि युक्त पुरुष पुण्य और पाप दोनों को इसी लोक में त्याग देता है। इसलिए तुम समत्वरूप योग में लग जाओ; कर्मों में कुशलता ही योग है।
        EXPLANATION: हिन्दी भावार्थ: "योगः कर्मसु कौशलम्" का अर्थ है कि पूर्ण समता और अनासक्ति के साथ कुशलतापूर्वक कर्म करना ही वास्तविक योग है।
        English Explanation: One who is engaged in devotional service rids himself of both good and bad reactions even in this life. Therefore, strive for Yoga, which is the art of all work.
    """.trimIndent()

    private val quranChapter1 = """
        VERSE_NO: 1
        ORIGINAL: بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
        TRANSLATION: शुरू करता हूँ अल्लाह के नाम से जो बड़ा मेहरबान और अत्यंत दयालु है।
        EXPLANATION: हिन्दी भावार्थ: हम हर नेक काम की शुरुआत अल्लाह के पावन नाम से करते हैं, जो असीम दयालु और कृपालु है।
        English Explanation: In the name of Allah, the Entirely Merciful, the Especially Merciful.
        ===
        VERSE_NO: 2
        ORIGINAL: الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ
        TRANSLATION: सब तारीफें अल्लाह ही के लिए हैं जो तमाम जहानों का पालने वाला है।
        EXPLANATION: हिन्दी भावार्थ: ब्रह्मांड की हर तारीफ और कृतज्ञता केवल उस एक परमात्मा (अल्लाह) के लिए है, जो सभी सृष्टियों का स्वामी है।
        English Explanation: [All] praise is [due] to Allah, Lord of the worlds.
        ===
        VERSE_NO: 3
        ORIGINAL: الرَّحْمَٰنِ الرَّحِيمِ
        TRANSLATION: जो बड़ा मेहरबान और अत्यंत दयालु है।
        EXPLANATION: हिन्दी भावार्थ: वह विधाता अत्यंत करुणामयी है, जिसकी दया दृष्टि सृष्टि के कण-कण पर बरसती है।
        English Explanation: The Entirely Merciful, the Especially Merciful.
        ===
        VERSE_NO: 4
        ORIGINAL: مَالِكِ يَوْمِ الدِّينِ
        TRANSLATION: न्याय के दिन का मालिक।
        EXPLANATION: हिन्दी भावार्थ: वह परमेश्वर कर्मों के प्रतिफल और अंतिम न्याय के दिन का एकमात्र न्यायधीश है।
        English Explanation: Sovereign of the Day of Recompense.
        ===
        VERSE_NO: 5
        ORIGINAL: إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ
        TRANSLATION: (हे अल्लाह!) हम तेरी ही इबादत करते हैं और तुझ ही से मदद चाहते हैं।
        EXPLANATION: हिन्दी भावार्थ: हम केवल एक निराकार प्रभु की आराधना करते हैं और जीवन के हर संकट में केवल उसी से सहायता मांगते हैं।
        English Explanation: It is You we worship and You we ask for help.
    """.trimIndent()

    private val quranChapter112 = """
        VERSE_NO: 1
        ORIGINAL: قُلْ هُوَ اللَّهُ أَحَدٌ
        TRANSLATION: कहो, वह अल्लाह एक (अकेला) है।
        EXPLANATION: हिन्दी भावार्थ: यह आयत इस्लाम के मूल सिद्धांत 'तौहीद' (एकेश्वरवाद) को स्पष्ट करती है—परमात्मा केवल एक ही है।
        English Explanation: Say, "He is Allah, [who is] One."
        ===
        VERSE_NO: 2
        ORIGINAL: اللَّهُ الصَّمَدُ
        TRANSLATION: अल्लाह बेनियाज़ है (सब उसके मोहताज हैं, वह किसी का मोहताज नहीं)।
        EXPLANATION: हिन्दी भावार्थ: वह ईश्वर परम स्वतंत्र और आत्मनिर्भर है। पूरी सृष्टि अपनी आवश्यकताओं के लिए उस पर निर्भर है।
        English Explanation: "Allah, the Absolute, Eternal (needed by all, needing none)."
        ===
        VERSE_NO: 3
        ORIGINAL: لَمْ يَلِدْ وَلَمْ يُولَدْ
        TRANSLATION: न उसकी कोई संतान है और न वह किसी की संतान है।
        EXPLANATION: हिन्दी भावार्थ: ईश्वर जन्म और मृत्यु के बंधनों से सर्वथा परे है। उसका न तो कोई माता-पिता है और न ही कोई वंशज।
        English Explanation: "He neither begets nor is born."
        ===
        VERSE_NO: 4
        ORIGINAL: وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ
        TRANSLATION: और न कोई उसके बराबर का है।
        EXPLANATION: हिन्दी भावार्थ: उस असीम परमात्मा के तुल्य अथवा समान इस संपूर्ण ब्रह्मांड में कोई दूसरा नहीं है।
        English Explanation: "And there is none co-equal or comparable unto Him."
    """.trimIndent()

    private val bibleGenesisChapter1 = """
        VERSE_NO: 1
        ORIGINAL: In the beginning, God created the heavens and the earth.
        TRANSLATION: आदि में परमेश्वर ने आकाश और पृथ्वी की सृष्टि की।
        EXPLANATION: हिन्दी भावार्थ: यह बाइबल की सबसे पहली आयत है जो घोषित करती है कि इस ब्रह्मांड और संपूर्ण प्रकृति का सृजनकर्ता एकमात्र ईश्वर ही है।
        English Explanation: In the beginning, God created the heavens and the earth. This is the foundational verse of the Bible establishing God as the supreme Creator of all physical reality.
        ===
        VERSE_NO: 2
        ORIGINAL: The earth was without form, and void; and darkness was on the face of the deep. And the Spirit of God was hovering over the face of the waters.
        TRANSLATION: और पृथ्वी बेडौल और सुनसान पड़ी थी, और गहरे जल के ऊपर अन्धियारा था; और परमेश्वर का आत्मा जल के ऊपर मण्डलाता था।
        EXPLANATION: हिन्दी भावार्थ: सृष्टि के निर्माण से पूर्व की स्थिति अंधकारमयी थी, और ईश्वर की चैतन्य आत्मा (पवित्र आत्मा) जल के ऊपर विद्यमान थी।
        English Explanation: The earth was without form, and void; and darkness was on the face of the deep. And the Spirit of God was hovering over the face of the waters.
        ===
        VERSE_NO: 3
        ORIGINAL: Then God said, "Let there be light"; and there was light.
        TRANSLATION: तब परमेश्वर ने कहा, “उजियाला हो,” और उजियाला हो गया।
        EXPLANATION: हिन्दी भावार्थ: ईश्वर की वाणी में अपार सामर्थ्य है। उनके मात्र शब्द कहने से ही ब्रह्मांड का प्रथम प्रकाश अस्तित्व में आ गया।
        English Explanation: Then God said, "Let there be light"; and there was light. It highlights the divine power of the Word of God in bringing existence out of nothingness.
    """.trimIndent()

    private val gurbaniJapjiSahib = """
        VERSE_NO: 1
        ORIGINAL: ੴ ਸਤਿ ਨਾਮੁ ਕਰਤਾ ਪੁਰਖੁ ਨਿਰਭਉ ਨਿਰਵੈਰੁ ਅਕਾਲ ਮੂਰਤਿ ਅਜੂਨੀ ਸੈਭੰ ਗੁਰ ਪ੍ਰਸਾਦਿ ॥
        TRANSLATION: एक अकाल पुरख (ईश्वर) है, जिसका नाम सत्य है, जो सृष्टि का रचयिता है, भय से रहित है, वैर-भाव से मुक्त है, समय से परे है, जन्म-मरण के बंधन से मुक्त है, स्वयंभू है और गुरु की कृपा से प्राप्त होता है।
        EXPLANATION: हिन्दी भावार्थ: यह सिखों का मूल मंत्र है, जो ईश्वर के निर्गुण और शाश्वत स्वरूप को अद्भुत रूप से प्रकट करता है।
        English Explanation: This is the 'Mool Mantar' of Sikhism: One Universal Creator God, The Supreme Truth, The Creator, Without Fear, Without Hate, Timeless, Unborn, Self-Existent, realized by the Guru's Grace.
        ===
        VERSE_NO: 2
        ORIGINAL: ॥ ਜਪੁ ॥ ਆਦਿ ਸਚੁ ਜੁਗਾਦਿ ਸਚੁ ॥ ਹੈ ਭੀ ਸਚੁ ਨਾਨਕ ਹੋਸੀ ਭੀ ਸਚੁ ॥੧॥
        TRANSLATION: वह परमेश्वर आदि काल से सत्य है, युगों के प्रारंभ में भी सत्य था, वर्तमान में भी वही सत्य है, और हे नानक! भविष्य में भी वही सत्य रहेगा।
        EXPLANATION: हिन्दी भावार्थ: संसार की हर वस्तु परिवर्तनशील और नश्वर है, केवल एक परमात्मा ही शाश्वत सत्य है।
        English Explanation: "True in the beginning, True through the ages, True even now, O Nanak, He shall forever be True."
    """.trimIndent()

    private val dhammapadaChapter1 = """
        VERSE_NO: 1
        ORIGINAL: मनोपुब्बङ्गमा धम्मा, मनोसिट्ठा मनोमया। मनसा चे पदुट्ठेन, भासति वा करोति वा। ततो नं दुक्खमन्वेति, चक्कं व वहतो पदं।।
        TRANSLATION: सभी मानसिक वृत्तियाँ मन की ही अगुआ हैं, मन ही उनका स्वामी है और वे मन से ही उत्पन्न होती हैं। यदि कोई दूषित मन से कोई बात बोलता है या कर्म करता है, तो दुःख उसका वैसे ही पीछा करता है जैसे बैल के पैर के पीछे गाड़ी का पहिया चलता है।
        EXPLANATION: हिन्दी भावार्थ: भगवान बुद्ध कर्म सिद्धांत की व्याख्या करते हुए बताते हैं कि हमारे सारे कर्मों की उत्पत्ति मन से होती है, इसलिए मन की पवित्रता ही परम सुख का आधार है।
        English Explanation: Mind precedes all mental states. Mind is their chief; they are mind-made. If one speaks or acts with an impure mind, suffering follows him even as the wheel follows the hoof of the draught-ox.
        ===
        VERSE_NO: 2
        ORIGINAL: मनोपुब्बङ्गमा धम्मा, मनोसिट्ठा मनोमया। मनसा चे पसन्नेन, भासति वा करोति वा। ततो नं सुखमन्वेति, छाया व अनपायिनी।।
        TRANSLATION: सभी मानसिक वृत्तियाँ मन की ही अगुआ हैं, मन ही उनका स्वामी है और वे मन से ही उत्पन्न होती हैं। यदि कोई शुद्ध व प्रसन्न मन से बोलता है या कर्म करता है, तो सुख उसकी छाया की तरह हमेशा उसके पीछे चलता है।
        EXPLANATION: हिन्दी भावार्थ: शुद्ध विचारों और सकारात्मक दृष्टिकोण से किया गया कर्म जीवन में सदा रहने वाले सुख और संतोष की वर्षा करता है।
        English Explanation: Mind precedes all mental states. Mind is their chief; they are mind-made. If one speaks or acts with a pure mind, happiness follows him like his shadow, never leaving him.
    """.trimIndent()

    private val vedaChapter1 = """
        VERSE_NO: 1
        ORIGINAL: अग्निमीळे पुरोहितं यज्ञस्य देवमृत्विजम् । होतारं रत्नधातमम् ॥ १.००१.०१
        TRANSLATION: मैं यज्ञ के पुरोहित, प्रकाशमान देव, ऋतु के अनुसार यज्ञ कराने वाले, देवताओं का आह्वान करने वाले और रत्नों को धारण करने वाले अग्निदेव की स्तुति करता हूँ।
        EXPLANATION: हिन्दी भावार्थ: यह संपूर्ण ऋग्वेद का सर्वप्रथम मंत्र है, जिसमें कल्याणकारी अग्निदेव की आराधना की गई है जो ज्ञान और प्रकाश का मूल स्रोत हैं।
        English Explanation: "I praise Agni, the chosen Priest of the sacrifice, the divine Ministrant, the Summoner, who bestows the greatest wealth of spiritual gems." This is the opening verse of Rigveda.
        ===
        VERSE_NO: 2
        ORIGINAL: तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि धियो यो नः प्रचोदयात ॥
        TRANSLATION: हम सृष्टि के निर्माता उस दिव्य सविता देव के अति श्रेष्ठ तेज का ध्यान करते हैं, जो हमारी बुद्धियों को सत्कर्मों की ओर प्रेरित करे।
        EXPLANATION: हिन्दी भावार्थ: यह गायत्री महामंत्र है, जो जीवन में सद्बुद्धि, आत्मिक प्रकाश, और ज्ञान की प्राप्ति की प्रार्थना करता है।
        English Explanation: The sacred Gayatri Mantra from the Rigveda, praying for intellect, divine wisdom, and spiritual illumination.
        ===
        VERSE_NO: 3
        ORIGINAL: सं गच्छध्वं सं वदध्वं सं वो मनांसि जानताम् । देवा भागं यथा पूर्वे संजानाना उपासते ॥
        TRANSLATION: तुम सब एक साथ चलो, एक साथ बोलो, तुम्हारे मन एक समान होकर ज्ञान प्राप्त करें। जैसे प्राचीन काल में ज्ञानी जन एकमत होकर अपना कर्तव्य पूर्ण करते थे।
        EXPLANATION: हिन्दी भावार्थ: ऋग्वेद का यह संगठन सूक्त समाज में लोकतांत्रिक एकता, सामूहिक चिंतन, और विकास की सुंदर शिक्षा देता है।
        English Explanation: Inspires collective action, unity of purpose, speech, and intellectual collaboration for the betterment of society.
        ===
        VERSE_NO: 4
        ORIGINAL: द्युलोकः शान्तिरन्तरिक्षं शान्तिः पृथिवी शान्तिरापः शान्तिरोषधयः शान्तिः ||
        TRANSLATION: द्युलोक में शांति हो, अंतरिक्ष में शांति हो, पृथ्वी पर शांति हो, जल में शांति हो, वनस्पति और औषधियां शांत और कल्याणकारी हों।
        EXPLANATION: हिन्दी भावार्थ: यजुर्वेद का यह शांति पाठ संपूर्ण ब्रह्मांड, प्रकृति और मानव जाति के बीच परम सामंजस्य और सद्भाव की कामना करता है।
        English Explanation: A Vedic prayer invoking supreme peace and harmony throughout the universe, nature, and all living beings.
        ===
        VERSE_NO: 5
        ORIGINAL: सहृदयं सांमनस्यमविद्वेषं कृणोमि वः | अन्यो अन्यमभि हर्यत वत्सं जातमिव आघ्न्या ||
        TRANSLATION: मैं आप सभी के हृदयों को समान विचार वाला, द्वेषरहित और प्रेमपूर्ण बनाता हूँ। आप आपस में वैसा ही प्रेम करें जैसा गाय अपने बछड़े से करती है।
        EXPLANATION: हिन्दी भावार्थ: अथर्ववेद का यह मंत्र पारिवारिक और सामाजिक एकता, आपसी प्रेम, भाईचारे और अहिंसा का दिव्य संदेश देता है।
        English Explanation: Encourages humanity to live in loving cooperation, harmony, mutual respect, and pure affection.
    """.trimIndent()
}
