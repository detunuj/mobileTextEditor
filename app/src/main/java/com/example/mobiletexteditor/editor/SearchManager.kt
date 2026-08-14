/**
 * File: SearchManager.kt
 * Purpose: Text search and replace engine. Implements case-sensitive / whole-word pattern searches,
 *          forward/backward match navigation, single replacement, and bulk replace-all operations.
 * Group Member: Member 1 — Editor Engine & File Management
 */
package com.example.mobiletexteditor.editor

import com.example.mobiletexteditor.editor.model.SearchMatch
import com.example.mobiletexteditor.editor.model.SearchResult
import java.util.regex.Pattern

/**
 * Text search and replace engine with case-sensitivity and whole-word matching.
 */
object SearchManager {

    /**
     * Executes a search for [query] across [text] with optional filters.
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
        while (matcher.find()) {
            matches.add(
                SearchMatch(
                    startIndex = matcher.start(),
                    endIndex = matcher.end(),
                    matchedText = matcher.group()
                )
            )
        }

        val activeIndex = if (matches.isNotEmpty()) 0 else -1

        return SearchResult(
            query = query,
            matches = matches,
            currentMatchIndex = activeIndex,
            isCaseSensitive = isCaseSensitive,
            isMatchWholeWord = isMatchWholeWord
        )
    }

    /**
     * Advances to the next match occurrence.
     */
    fun findNext(currentResult: SearchResult): SearchResult {
        if (!currentResult.hasMatches) return currentResult
        val nextIndex = (currentResult.currentMatchIndex + 1) % currentResult.totalMatches
        return currentResult.copy(currentMatchIndex = nextIndex)
    }

    /**
     * Moves to the previous match occurrence.
     */
    fun findPrevious(currentResult: SearchResult): SearchResult {
        if (!currentResult.hasMatches) return currentResult
        val prevIndex = if (currentResult.currentMatchIndex <= 0) {
            currentResult.totalMatches - 1
        } else {
            currentResult.currentMatchIndex - 1
        }
        return currentResult.copy(currentMatchIndex = prevIndex)
    }

    /**
     * Replaces the currently focused match with [replacement] and returns the updated text.
     */
    fun replaceCurrent(
        text: String,
        searchResult: SearchResult,
        replacement: String
    ): Pair<String, SearchResult> {
        val activeMatch = searchResult.activeMatch ?: return Pair(text, searchResult)

        val newText = text.substring(0, activeMatch.startIndex) +
                replacement +
                text.substring(activeMatch.endIndex)

        // Re-execute search on new text
        val updatedSearchResult = search(
            text = newText,
            query = searchResult.query,
            isCaseSensitive = searchResult.isCaseSensitive,
            isMatchWholeWord = searchResult.isMatchWholeWord
        )

        // Maintain relative match index
        val newIndex = if (updatedSearchResult.hasMatches) {
            searchResult.currentMatchIndex.coerceAtMost(updatedSearchResult.totalMatches - 1)
        } else {
            -1
        }

        return Pair(newText, updatedSearchResult.copy(currentMatchIndex = newIndex))
    }

    /**
     * Replaces all match occurrences in [text] with [replacement].
     *
     * @return Pair(updatedText, totalReplacedCount)
     */
    fun replaceAll(
        text: String,
        query: String,
        replacement: String,
        isCaseSensitive: Boolean = false,
        isMatchWholeWord: Boolean = false
    ): Pair<String, Int> {
        if (query.isEmpty() || text.isEmpty()) return Pair(text, 0)

        val patternString = if (isMatchWholeWord) {
            "\\b${Pattern.quote(query)}\\b"
        } else {
            Pattern.quote(query)
        }

        val flags = if (isCaseSensitive) 0 else Pattern.CASE_INSENSITIVE
        val pattern = Pattern.compile(patternString, flags)
        val matcher = pattern.matcher(text)

        val count = search(text, query, isCaseSensitive, isMatchWholeWord).totalMatches
        val newText = matcher.replaceAll(replacement)

        return Pair(newText, count)
    }
}
