@@ -14,6 +14,7 @@
 import androidx.compose.foundation.layout.width
 import androidx.compose.foundation.lazy.LazyColumn
 import androidx.compose.foundation.lazy.LazyRow
+import androidx.compose.foundation.ExperimentalFoundationApi
 import androidx.compose.foundation.lazy.items
 import androidx.compose.foundation.shape.CircleShape
 import androidx.compose.foundation.shape.RoundedCornerShape
@@ -53,7 +54,8 @@
 import com.example.data.localization.LocalizationManager
 import com.example.ui.theme.WarmGoldAccent
 
+@OptIn(ExperimentalFoundationApi::class)
 @Composable
 fun DuaScreen(
   allDuas: List<com.example.data.dua.DuaItem>,
@@ -211,18 +213,38 @@
         }
       }
     } else {
+      val groupedDuas = remember(filteredDuas, language) {
+         filteredDuas.groupBy {
+           if (it.id.startsWith("hisn_")) it.title else it.category.getLocalizedName(language)
+         }
+      }
+
       LazyColumn(
         modifier = Modifier
           .fillMaxSize()
           .testTag("duas_list"),
         verticalArrangement = Arrangement.spacedBy(14.dp)
       ) {
-        items(filteredDuas, key = { it.id }) { dua ->
-          val isBookmarked = bookmarkedDuaIds.contains(dua.id)
-          DuaCardItem(
-            dua = dua,
-            isBookmarked = isBookmarked,
-            language = language,
-            onToggleBookmark = { onToggleBookmark(dua.id) }
-          )
-        }
+        groupedDuas.forEach { (groupTitle, duas) ->
+          stickyHeader {
+            Surface(
+              color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
+              modifier = Modifier.fillMaxWidth()
+            ) {
+              Text(
+                text = groupTitle,
+                style = MaterialTheme.typography.titleMedium,
+                fontWeight = FontWeight.Bold,
+                color = WarmGoldAccent,
+                modifier = Modifier.padding(vertical = 8.dp)
+              )
+            }
+          }
+          items(duas, key = { it.id }) { dua ->
+            val isBookmarked = bookmarkedDuaIds.contains(dua.id)
+            DuaCardItem(
+              dua = dua,
+              isBookmarked = isBookmarked,
+              language = language,
+              onToggleBookmark = { onToggleBookmark(dua.id) }
+            )
+          }
+        }
         item {
           Spacer(modifier = Modifier.height(24.dp))
