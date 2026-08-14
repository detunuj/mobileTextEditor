package com.example.mobiletexteditor.editor

import com.example.mobiletexteditor.editor.model.SearchMatch
import com.example.mobiletexteditor.editor.model.SearchResult
import java.util.regex.Pattern

/**
 * Manages search, search-and-replace, and match navigation.
 */
object SearchManager {

    /**
     * Executes a search query on the provided [text].
     */
    fun search(
        text: String,
        query: String,
        isCaseSensitive: Boolean = false,
        isMatchWholeWord: Boolean = false
    ): SearchResult {
        if (query.isEmpty() || text.isEmpty()) {
            return SearchResult(query = query, isCaseSensitive = isCaseSensitive, isMatchWholeWord = isMatchWholeWord)
        }

        val patternString = if (isMatchWholeWord) {
            "\\b${Pattern.quote(query)}\\b"
        } else {
            Pattern.quote(query)
        }

        val flags = if (isCaseSensitive) 0 else Pattern.CASE_INSENSITIVE
        val pattern = Pattern.compile(patternString, flags)
        val matcher = pattern.matcher(text)

        val matches = mutableListOf<SearchMatch>()
        val lines = text.split("\n")

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            // Calculate line number
            val prefix = text.substring(0, start)
            val lineNumber = prefix.count { it == '\n' } + 1
            val lineText = if (lineNumber - 1 in lines.indices) lines[lineNumber - 1] else ""

            matches.add(
                SearchMatch(
                    startIndex = start,
                    endIndex = end,
                    lineNumber = lineNumber,
                    lineText = lineText
                )
            )
        }

        return SearchResult(
            query = query,
            matches = matches,
            currentMatchIndex = if (matches.isNotEmpty()) 0 else -1,
            isCaseSensitive = isCaseSensitive,
            isMatchWholeWord = isMatchWholeWord
        )
    }

    /**
     * Advances to the next match in the search results list.
     */
    fun findNext(currentResult: SearchResult): SearchResult {
        if (!currentResult.hasMatches) return currentResult

        val nextIndex = (currentResult.currentMatchIndex + 1) % currentResult.matches.size
        return currentResult.copy(currentMatchIndex = nextIndex)
    }

    /**
     * Reverses to the previous match in the search results list.
     */
    fun findPrevious(currentResult: SearchResult): SearchResult {
        if (!currentResult.hasMatches) return currentResult

        val prevIndex = if (currentResult.currentMatchIndex <= 0) {
            currentResult.matches.size - 1
        } else {
            currentResult.currentMatchIndex - 1
        }
        return currentResult.copy(currentMatchIndex = prevIndex)
    }

    /**
     * Replaces the currently selected match with [replacement] text.
     *
     * @return Pair(updatedText, updatedSearchResult)
     */
    fun replaceCurrent(
        text: String,
        searchResult: SearchResult,
        replacement: String
    ): Pair<String, SearchResult> {
        val activeMatch = searchResult.activeMatch ?: return Pair(text, searchResult)

        val newText = StringBuilder(text)
            .replace(activeMatch.startIndex, activeMatch.endIndex, replacement)
            .toString()

        val updatedResult = search(
            text = newText,
            query = searchResult.query,
            isCaseSensitive = searchResult.isCaseSensitive,
            isMatchWholeWord = searchResult.isMatchWholeWord
        )

        // Try to maintain approximate match index
        val targetIndex = searchResult.currentMatchIndex.coerceAtMost(updatedResult.matches.size - 1)
        return Pair(newText, updatedResult.copy(currentMatchIndex = targetIndex.coerceAtLeast(if (updatedResult.hasMatches) 0 else -1)))
    }

    /**
     * Replaces all occurrences of [query] with [replacement].
     *
     * @return Pair(updatedText, countOfReplacements)
     */
    fun replaceAll(
        text: String,
        query: String,
        replacement: String,
        isCaseSensitive: Boolean = false,
        isMatchWholeWord: Boolean = false
    ): Pair<String, Int> {
        if (query.isEmpty() || text.isEmpty()) {
            return Pair(text, 0)
        }

        val patternString = if (isMatchWholeWord) {
            "\\b${Pattern.quote(query)}\\b"
        } else {
            Pattern.quote(query)
        }

        val flags = if (isCaseSensitive) 0 else Pattern.CASE_INSENSITIVE
        val pattern = Pattern.compile(patternString, flags)
        val matcher = pattern.matcher(text)

        var count = 0
        val sb = StringBuffer()
        while (matcher.find()) {
            matcher.appendReplacement(sb, MatcherQuoteReplacement(replacement))
            count++
        }
        matcher.appendTail(sb)

        return Pair(sb.toString(), count)
    }

    private fun MatcherQuoteReplacement(s: String): String {
        return java.util.regex.Matcher.quoteReplacement(s)
    }
}
