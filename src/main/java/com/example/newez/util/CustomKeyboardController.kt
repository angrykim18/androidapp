package com.example.newez.util

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.newez.R

class CustomKeyboardController(
    private val keyboardView: View,
    private val targetEditText: EditText
) {
    private var isShifted = false
    private var isKorean = true

    // ✅ [수정] 한글 조합을 위한 상수 및 데이터 구조
    private val CHO = charArrayOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    private val JUN = charArrayOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
    private val JON = charArrayOf(' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')

    private val JON_DOUBLE = mapOf(
        'ㄳ' to ('ㄱ' to 'ㅅ'), 'ㄵ' to ('ㄴ' to 'ㅈ'), 'ㄶ' to ('ㄴ' to 'ㅎ'),
        'ㄺ' to ('ㄹ' to 'ㄱ'), 'ㄻ' to ('ㄹ' to 'ㅁ'), 'ㄼ' to ('ㄹ' to 'ㅂ'),
        'ㄽ' to ('ㄹ' to 'ㅅ'), 'ㄾ' to ('ㄹ' to 'ㅌ'), 'ㄿ' to ('ㄹ' to 'ㅍ'),
        'ㅀ' to ('ㄹ' to 'ㅎ'), 'ㅄ' to ('ㅂ' to 'ㅅ')
    )

    private val JUN_DOUBLE = mapOf(
        'ㅘ' to ('ㅗ' to 'ㅏ'), 'ㅙ' to ('ㅗ' to 'ㅐ'), 'ㅚ' to ('ㅗ' to 'ㅣ'),
        'ㅝ' to ('ㅜ' to 'ㅓ'), 'ㅞ' to ('ㅜ' to 'ㅔ'), 'ㅟ' to ('ㅜ' to 'ㅣ'),
        'ㅢ' to ('ㅡ' to 'ㅣ')
    )

    private val keyMap = mapOf(
        R.id.button_key_1 to "1", R.id.button_key_2 to "2", R.id.button_key_3 to "3",
        R.id.button_key_4 to "4", R.id.button_key_5 to "5", R.id.button_key_6 to "6",
        R.id.button_key_7 to "7", R.id.button_key_8 to "8", R.id.button_key_9 to "9",
        R.id.button_key_0 to "0",

        R.id.button_key_B to Pair('ㅂ', 'ㅃ'), R.id.button_key_J to Pair('ㅈ', 'ㅉ'),
        R.id.button_key_D to Pair('ㄷ', 'ㄸ'), R.id.button_key_G to Pair('ㄱ', 'ㄲ'),
        R.id.button_key_S to Pair('ㅅ', 'ㅆ'), R.id.button_key_YO to Pair('ㅛ', 'ㅛ'),
        R.id.button_key_YEO to Pair('ㅕ', 'ㅕ'), R.id.button_key_YA to Pair('ㅑ', 'ㅑ'),
        R.id.button_key_AE to Pair('ㅐ', 'ㅒ'), R.id.button_key_E to Pair('ㅔ', 'ㅖ'),

        R.id.button_key_M to Pair('ㅁ', 'ㅁ'), R.id.button_key_N to Pair('ㄴ', 'ㄴ'),
        R.id.button_key_O to Pair('ㅇ', 'ㅇ'), R.id.button_key_R to Pair('ㄹ', 'ㄹ'),
        R.id.button_key_H to Pair('ㅎ', 'ㅎ'), R.id.button_key_HO to Pair('ㅗ', 'ㅗ'),
        R.id.button_key_HEO to Pair('ㅓ', 'ㅓ'), R.id.button_key_HA to Pair('ㅏ', 'ㅏ'),
        R.id.button_key_HI to Pair('ㅣ', 'ㅣ'),

        R.id.button_key_K to Pair('ㅋ', 'ㅋ'), R.id.button_key_T to Pair('ㅌ', 'ㅌ'),
        R.id.button_key_CH to Pair('ㅊ', 'ㅊ'), R.id.button_key_P to Pair('ㅍ', 'ㅍ'),
        R.id.button_key_YU to Pair('ㅠ', 'ㅠ'), R.id.button_key_U to Pair('ㅜ', 'ㅜ'),
        R.id.button_key_EU to Pair('ㅡ', 'ㅡ')
    )

    private val engKeyMap = mapOf(
        R.id.button_key_B to "q", R.id.button_key_J to "w", R.id.button_key_D to "e",
        R.id.button_key_G to "r", R.id.button_key_S to "t", R.id.button_key_YO to "y",
        R.id.button_key_YEO to "u", R.id.button_key_YA to "i", R.id.button_key_AE to "o",
        R.id.button_key_E to "p", R.id.button_key_M to "a", R.id.button_key_N to "s",
        R.id.button_key_O to "d", R.id.button_key_R to "f", R.id.button_key_H to "g",
        R.id.button_key_HO to "h", R.id.button_key_HEO to "j", R.id.button_key_HA to "k",
        R.id.button_key_HI to "l", R.id.button_key_K to "z", R.id.button_key_T to "x",
        R.id.button_key_CH to "c", R.id.button_key_P to "v", R.id.button_key_YU to "b",
        R.id.button_key_U to "n", R.id.button_key_EU to "m"
    )

    init {
        setupButtonListeners()
        updateKeyboard()
    }

    private fun setupButtonListeners() {
        (keyMap.keys + engKeyMap.keys + listOf(
            R.id.button_key_1, R.id.button_key_2, R.id.button_key_3, R.id.button_key_4, R.id.button_key_5,
            R.id.button_key_6, R.id.button_key_7, R.id.button_key_8, R.id.button_key_9, R.id.button_key_0
        )).distinct().forEach { keyId ->
            keyboardView.findViewById<TextView>(keyId)?.setOnClickListener { onKeyPress(keyId) }
        }

        keyboardView.findViewById<TextView>(R.id.button_key_shift).setOnClickListener { onShiftPress() }
        keyboardView.findViewById<TextView>(R.id.button_key_lang).setOnClickListener { onLangPress() }
        keyboardView.findViewById<TextView>(R.id.button_key_space).setOnClickListener { onSpacePress() }
        keyboardView.findViewById<TextView>(R.id.button_key_backspace).setOnClickListener { onBackspacePress() }
    }

    // ✅ [수정] 한글 조합 로직이 포함된 새로운 onKeyPress 메소드
    private fun onKeyPress(keyId: Int) {
        val text = targetEditText.text.toString()
        val charToInsert = getCharFromKey(keyId)

        if (isKorean && charToInsert != null) {
            composeHangul(text, charToInsert)
        } else if (charToInsert != null) {
            targetEditText.append(charToInsert.toString())
        }
    }

    private fun onShiftPress() {
        isShifted = !isShifted
        updateKeyboard()
    }

    private fun onLangPress() {
        isKorean = !isKorean
        isShifted = false
        updateKeyboard()
    }

    private fun onSpacePress() {
        targetEditText.append(" ")
    }

    // ✅ [수정] 한글 분해 로직이 포함된 새로운 onBackspacePress 메소드
    private fun onBackspacePress() {
        val text = targetEditText.text.toString()
        if (text.isEmpty()) return

        val lastChar = text.last()
        if (isKorean && lastChar in '가'..'힣') {
            val (cho, jun, jon) = decompose(lastChar)
            if (JON_DOUBLE.containsKey(JON[jon])) {
                val (first, _) = JON_DOUBLE[JON[jon]]!!
                setText(text.dropLast(1) + compose(cho, jun, JON.indexOf(first)))
            } else if (jon > 0) {
                setText(text.dropLast(1) + compose(cho, jun, 0))
            } else if (JUN_DOUBLE.containsKey(JUN[jun])) {
                val (first, _) = JUN_DOUBLE[JUN[jun]]!!
                setText(text.dropLast(1) + compose(cho, JUN.indexOf(first), 0))
            } else {
                setText(text.dropLast(1) + CHO[cho])
            }
        } else {
            setText(text.dropLast(1))
        }
    }

    private fun updateKeyboard() {
        keyMap.forEach { (keyId, value) ->
            keyboardView.findViewById<TextView>(keyId)?.let { button ->
                if (value is Pair<*, *>) {
                    val text = if (isKorean) {
                        if (isShifted) (value.second as Char).toString() else (value.first as Char).toString()
                    } else {
                        val engChar = engKeyMap[keyId] ?: ""
                        if (isShifted) engChar.uppercase() else engChar
                    }
                    button.text = text
                }
            }
        }
    }

    private fun getCharFromKey(keyId: Int): Char? {
        if (isKorean) {
            val keyData = keyMap[keyId]
            return when (keyData) {
                is String -> keyData.first()
                is Pair<*, *> -> if (isShifted) keyData.second as Char else keyData.first as Char
                else -> null
            }
        } else {
            val keyData = engKeyMap[keyId]
            val numData = keyMap[keyId] as? String
            val char = keyData ?: numData ?: return null
            return if (isShifted) char.uppercase().first() else char.first()
        }
    }

    private fun setText(text: String) {
        targetEditText.setText(text)
        targetEditText.setSelection(text.length)
    }

    // --- 한글 조합 로직 ---
    private fun isChosung(c: Char) = c in 'ㄱ'..'ㅎ'
    private fun isJungsung(c: Char) = c in 'ㅏ'..'ㅣ'

    private fun composeHangul(text: String, newJamo: Char) {
        if (text.isEmpty()) {
            setText(newJamo.toString())
            return
        }
        val lastChar = text.last()
        if (isJungsung(newJamo)) {
            if (isChosung(lastChar)) {
                setText(text.dropLast(1) + compose(CHO.indexOf(lastChar), JUN.indexOf(newJamo), 0))
            } else if (lastChar in '가'..'힣') {
                val (cho, jun, jon) = decompose(lastChar)
                if (jon > 0) {
                    val (first, second) = JON_DOUBLE[JON[jon]] ?: (JON[jon] to null)
                    if (second != null) {
                        val newLastChar = compose(cho, jun, JON.indexOf(first))
                        val newSyllable = compose(CHO.indexOf(second), JUN.indexOf(newJamo), 0)
                        setText(text.dropLast(1) + newLastChar + newSyllable)
                    } else {
                        val newSyllable = compose(CHO.indexOf(JON[jon]), JUN.indexOf(newJamo), 0)
                        setText(text.dropLast(1) + compose(cho, jun, 0) + newSyllable)
                    }
                } else {
                    val combinedVowel = JUN.indexOfFirst { JUN_DOUBLE[it]?.first == JUN[jun] && JUN_DOUBLE[it]?.second == newJamo }.takeIf { it != -1 }
                    if (combinedVowel != null) {
                        setText(text.dropLast(1) + compose(cho, combinedVowel, 0))
                    } else {
                        setText(text + newJamo)
                    }
                }
            } else {
                setText(text + newJamo)
            }
        } else { // 자음 입력
            if (lastChar in '가'..'힣') {
                val (cho, jun, jon) = decompose(lastChar)
                if (jon == 0) {
                    val jonIdx = JON.indexOf(newJamo).takeIf { it != -1 }
                    if (jonIdx != null) {
                        setText(text.dropLast(1) + compose(cho, jun, jonIdx))
                    } else {
                        setText(text + newJamo)
                    }
                } else {
                    val combinedConsonant = JON.indexOfFirst { JON_DOUBLE[it]?.first == JON[jon] && JON_DOUBLE[it]?.second == newJamo }.takeIf { it != -1 }
                    if (combinedConsonant != null) {
                        setText(text.dropLast(1) + compose(cho, jun, combinedConsonant))
                    } else {
                        setText(text + newJamo)
                    }
                }
            } else {
                setText(text + newJamo)
            }
        }
    }
    private fun compose(cho: Int, jun: Int, jon: Int): Char = (0xAC00 + cho * 21 * 28 + jun * 28 + jon).toChar()
    private fun decompose(syl: Char): Triple<Int, Int, Int> {
        val unicode = syl.code - 0xAC00
        val cho = unicode / (21 * 28)
        val jun = (unicode % (21 * 28)) / 28
        val jon = unicode % 28
        return Triple(cho, jun, jon)
    }
}