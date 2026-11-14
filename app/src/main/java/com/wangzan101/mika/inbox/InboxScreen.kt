package com.wangzan101.mika.inbox

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wangzan101.mika.ui.components.ButtonKind
import com.wangzan101.mika.ui.components.ButtonSize
import com.wangzan101.mika.ui.components.GroupCard
import com.wangzan101.mika.ui.components.GroupLabel
import com.wangzan101.mika.ui.components.MikaButton
import com.wangzan101.mika.ui.components.MikaCard
import com.wangzan101.mika.ui.components.PageScaffold
import com.wangzan101.mika.ui.components.PillTone
import com.wangzan101.mika.ui.components.StatusPill
import com.wangzan101.mika.ui.theme.MikaShapes
import com.wangzan101.mika.ui.theme.MikaSize
import com.wangzan101.mika.ui.theme.MikaTheme
import com.wangzan101.mika.ui.theme.MikaType

// TODO(i18n): 迁移到 string 资源

/** 一条收件箱:对方说了什么、助手拟好没有、处理了没有。 */
private enum class InboxKind { Drafted, Waiting, Ignored }

private data class InboxRow(
    val id: String,
    val initial: String,
    val name: String,
    val message: String,
    val tag: String,
    val kind: InboxKind,
    val time: String,
    val draft: String,
    val handled: Boolean = false,
)

private val InboxRows = listOf(
    InboxRow(
        id = "wang", initial = "王", name = "王姐", message = "方案今天能发我吗？",
        tag = "已拟稿", kind = InboxKind.Drafted, time = "刚刚",
        draft = "王姐，方案今天下班前一定发您，发完我同步进度~",
    ),
    InboxRow(
        id = "mom", initial = "妈", name = "妈妈", message = "周末回来吃饭吗",
        tag = "等你定", kind = InboxKind.Waiting, time = "12 分钟前",
        draft = "妈，这周末我回来，想吃什么提前跟我说~",
    ),
    InboxRow(
        id = "li", initial = "李", name = "李萌", message = "那个视频我看了，绝了",
        tag = "已拟稿", kind = InboxKind.Drafted, time = "1 小时前",
        draft = "哈哈是吧，我也笑了一晚上，回头再给你推几个~",
    ),
    InboxRow(
        id = "kuaidi", initial = "快", name = "快递驿站", message = "取件码 8-2-1107",
        tag = "已忽略", kind = InboxKind.Ignored, time = "昨天", draft = "", handled = true,
    ),
)

/** 收件箱入口:待处理的逐条确认,点一行进详情,详情里的返回回到列表(都在这一页内)。 */
@Composable
fun InboxScreen(onBack: () -> Unit) {
    var selected by remember { mutableStateOf<InboxRow?>(null) }
    // 详情是页内状态:系统返回先回列表,再按才退出这一页
    BackHandler(enabled = selected != null) { selected = null }
    val row = selected
    if (row == null) {
        InboxList(onBack = onBack, onOpen = { selected = it })
    } else {
        InboxDetail(row, onBack = { selected = null })
    }
}

@Composable
private fun InboxList(onBack: () -> Unit, onOpen: (InboxRow) -> Unit) {
    val c = MikaTheme.colors
    val pending = InboxRows.filter { !it.handled }
    val handled = InboxRows.filter { it.handled }
    PageScaffold("收件箱", onBack) {
        Text("${pending.size} 条待处理", Modifier.padding(top = 8.dp), style = MikaType.LargeTitle, color = c.ink)
        Text("像处理邮件一样，逐条确认", Modifier.padding(top = 2.dp), style = MikaType.Caption, color = c.ink2)
        GroupLabel("待处理")
        GroupCard {
            pending.forEachIndexed { index, row -> InboxRowItem(row, first = index == 0, onOpen = { onOpen(row) }) }
        }
        GroupLabel("已处理")
        GroupCard {
            handled.forEachIndexed { index, row -> InboxRowItem(row, first = index == 0, onOpen = { onOpen(row) }) }
        }
    }
}

@Composable
private fun InboxRowItem(row: InboxRow, first: Boolean, onOpen: () -> Unit) {
    val c = MikaTheme.colors
    Row(
        Modifier.fillMaxWidth()
            .topHairline(first, 58.dp, c.hair)
            .clickable(role = Role.Button, onClick = onOpen)
            .heightIn(min = MikaSize.RowMin)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(MikaSize.Tile).background(c.neutralSoft, MikaShapes.Tile),
            contentAlignment = Alignment.Center,
        ) {
            Text(row.initial, style = MikaType.Body, fontWeight = FontWeight.SemiBold, color = c.ink)
        }
        Column(Modifier.weight(1f)) {
            Text(
                row.name, style = MikaType.BodyStrong, color = if (row.handled) c.ink2 else c.ink,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                row.message, Modifier.padding(top = 2.dp), style = MikaType.Caption,
                color = if (row.handled) c.ink3 else c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatusPill(row.tag, toneOf(row.kind))
            Text(row.time, style = MikaType.Hint, color = c.ink2, maxLines = 1, softWrap = false)
        }
    }
}

private fun toneOf(kind: InboxKind): PillTone = when (kind) {
    InboxKind.Drafted -> PillTone.Accent
    InboxKind.Waiting -> PillTone.Warn
    InboxKind.Ignored -> PillTone.Neutral
}

@Composable
private fun InboxDetail(row: InboxRow, onBack: () -> Unit) {
    val c = MikaTheme.colors
    // 按钮只改这一句状态文案,不真发消息
    var action by remember(row.id) { mutableStateOf<String?>(null) }
    PageScaffold(row.name, onBack) {
        GroupLabel("对方消息")
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text(
                row.message,
                Modifier.widthIn(max = 300.dp).background(c.answer, MikaShapes.AnswerBubble)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                style = MikaType.Body, color = c.ink,
            )
        }
        GroupLabel("拟稿")
        MikaCard {
            Column(Modifier.padding(16.dp)) {
                Text("拟稿 · 待确认", style = MikaType.Label, color = c.ink3)
                Text(
                    row.draft.ifEmpty { "这条不需要回复" }, Modifier.padding(top = 8.dp),
                    style = MikaType.Body, color = c.ink,
                )
                action?.let {
                    Text(it, Modifier.padding(top = 10.dp), style = MikaType.Hint, color = c.accent)
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MikaButton(
                        "发送", { action = "已确认发送（静态演示）" }, Modifier.weight(1f),
                        kind = ButtonKind.Primary, size = ButtonSize.Medium,
                    )
                    MikaButton(
                        "修改", { action = "已打开修改（静态演示）" }, Modifier.weight(1f),
                        kind = ButtonKind.Tonal, size = ButtonSize.Medium,
                    )
                    MikaButton(
                        "忽略", { action = "已忽略这条（静态演示）" }, Modifier.weight(1f),
                        kind = ButtonKind.Text, size = ButtonSize.Medium,
                    )
                }
            }
        }
    }
}

/** 行与行之间的分隔线,从 [start] 画到右边;分组首行([first])不画。 */
private fun Modifier.topHairline(first: Boolean, start: Dp, color: Color): Modifier =
    if (first) this
    else drawBehind {
        val y = 0.25.dp.toPx()
        drawLine(color, Offset(start.toPx(), y), Offset(size.width, y), strokeWidth = 0.5.dp.toPx())
    }
