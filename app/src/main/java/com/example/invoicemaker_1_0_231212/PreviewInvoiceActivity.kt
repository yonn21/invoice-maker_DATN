package com.example.invoicemaker_1_0_231212

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.invoicemaker_1_0_231212.ads.AdmobInterstitialAd
import com.example.invoicemaker_1_0_231212.ads.InterstitialAd_Key_Count
import com.example.invoicemaker_1_0_231212.utils.PreferencesManager
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Div
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.layout.LayoutArea
import com.itextpdf.layout.layout.LayoutContext
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ceil


class PreviewInvoiceActivity : BaseActivity() {
    private lateinit var backButton: Button
    private lateinit var shareButton: Button
    private lateinit var pdfView: PDFView

    private var invoice : JSONObject? = null
    private var businessData : JSONObject? = null

    private var pdfFile : File? = null

    private lateinit var settings: JSONObject
    private lateinit var currencyPosition: String // before or after
    private lateinit var currencySymbol: String
    private var decimalPlaces = 2
    private var numberFormat = 2
    private lateinit var dateFormatSetting: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_invoice)

        // read previewCount from preferences
        val preferences = getSharedPreferences("PREFERENCE", 0)
        val previewCount = preferences.getInt("previewCount", 0)

        // if previewCount is 1, show rating dialog
        if (previewCount == 1) {
            val bottomSheetSubscriptionDialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(R.layout.rating_bottom_sheet_dialog, null)
            bottomSheetSubscriptionDialog.setContentView(view)

            bottomSheetSubscriptionDialog.show()
        }

        settings = readSettings()
        currencyPosition = settings.getString("currencyPosition")
        currencySymbol = settings.getString("currencySymbol")
        decimalPlaces = settings.getInt("decimalPlaces")
        numberFormat = settings.getInt("numberFormat")
        dateFormatSetting = settings.getString("dateFormat")

        shareButton = findViewById(R.id.preview_invoice_share_button)
        backButton = findViewById(R.id.preview_invoice_back_button)

        // read SHARE in intent
        val share = intent.getBooleanExtra("SHARE", false)
        println("SHARE: $share")

        // if previous screen is CreateInvoiceActivity, hide share button
        if (intent.getStringExtra("PREVIOUS_SCREEN") == "CreateInvoiceActivity") {

            shareButton.visibility = Button.GONE
            invoice = readTempInvoiceData()

        } else {

            shareButton.visibility = Button.VISIBLE
            // read invoice from InvoiceList.json
            invoice = readInvoice(intent.getStringExtra("INVOICE_ID")!!)

        }

        businessData = readBusinessData()

        pdfFile = generatePdfFromJson(invoice!!, businessData!!)

        pdfView = findViewById(R.id.pdfView)

        pdfView.fromFile(pdfFile)
            .enableSwipe(true)
            .enableDoubletap(true)
            .pageSnap(false)
            .spacing(10)
            .pageFling(false)
            .defaultPage(0)
            .load()

        // if SHARE is true, share pdf
        if (share) {
            // convert file PDF to Uri by FileProvider
            val pdfUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                pdfFile!!
            )

            // create share Intent
            Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val shareIntent = Intent.createChooser(this, "Share PDF via...")
                startActivity(shareIntent)
            }
        }

        shareButton.setOnClickListener {
            /*// convert file PDF to Uri by FileProvider
            val pdfUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                pdfFile!!
            )

            // create share Intent
            Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val shareIntent = Intent.createChooser(this, "Share PDF via...")
                startActivity(shareIntent)
            }*/

            if (PreferencesManager.checkSUB() != null) {
                // convert file PDF to Uri by FileProvider
                val pdfUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    pdfFile!!
                )

                // create share Intent
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val shareIntent = Intent.createChooser(this, "Share PDF via...")
                    startActivity(shareIntent)
                }
            } else {
                val shareCount = preferences.getInt("shareCount", 0)
                if (shareCount < 50) {
                    // set shareCount + 1
                    val editor = preferences.edit()
                    editor.putInt("shareCount", shareCount + 1)
                    editor.apply()

                    // convert file PDF to Uri by FileProvider
                    val pdfUri = FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.provider",
                        pdfFile!!
                    )

                    // create share Intent
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, pdfUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        val shareIntent = Intent.createChooser(this, "Share PDF via...")
                        startActivity(shareIntent)
                    }
                } else {
                    // show subscription_dialog
                    val bottomSheetSubscriptionDialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
                    val view = LayoutInflater.from(this).inflate(R.layout.subscription_dialog, null)
                    bottomSheetSubscriptionDialog.setContentView(view)

                    view.findViewById<Button>(R.id.Subscription_Button).setOnClickListener {
                        // go to SubscriptionActivity
                        val intent = Intent(this, SubscriptionActivity::class.java)
                        (this as PreviewInvoiceActivity).startActivityForResult(intent, 1)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        bottomSheetSubscriptionDialog.dismiss()
                    }

                    view.findViewById<Button>(R.id.Watch_ADS_Button).setOnClickListener {

                        bottomSheetSubscriptionDialog.dismiss()
                    }

                    bottomSheetSubscriptionDialog.show()
                }
            }
        }

        backButton.setOnClickListener {
            navigateBack()
        }
    }

    // Generate pdf from json
    @OptIn(ExperimentalStdlibApi::class)
    private fun generatePdfFromJson(invoice: JSONObject, businessData: JSONObject): File {

        // create InvoicePDF folder if not exists
        val invoicePdfFolder = File(filesDir, "InvoicePDF")
        if (!invoicePdfFolder.exists()) {
            invoicePdfFolder.mkdir()
        }

        // create pdf file in filesDir with invoice id as file name to InvoicePDF folder
        val pdfFile = File(filesDir, "InvoicePDF/invoice_${invoice.getString("id")}.pdf")

        // create pdf writer
        val writer = PdfWriter(pdfFile)

        // Initialize PDF document with A4 page size
        val pdfDocument = PdfDocument(writer)
        pdfDocument.setDefaultPageSize(PageSize.A4)

        // create document with A4 page size
        val document = Document(pdfDocument, PageSize.A4)

        // set margin top
        document.setTopMargin(180f)

        // setup fonts, colors, etc.

        // font bebas_neue
        val bebasFont: PdfFont = PdfFontFactory.createFont(
            "res/font/bebas.ttf",
            PdfEncodings.IDENTITY_H
        )

        // white color
        val white = DeviceRgb(255, 255, 255)

        // black color
        val black = DeviceRgb(0, 0, 0)

        // blue color
        val blue = DeviceRgb(27, 156, 252)

        // gray color
        val gray = DeviceRgb(128, 128, 128)

        // add content to document

        // ============================ add invoice details to document ============================

        // 567.6
        val itemColumnWidths = floatArrayOf(320.6f, 90f, 63f, 95f)
        val clientColumnWidths = floatArrayOf(320.6f, 90f, 63f, 95f)
        val paymentColumnWidths = floatArrayOf(337.6f, 30.5f, 115f, 84.5f)
        var itemListTable = Table(itemColumnWidths)
        var clientInfoTable = Table(clientColumnWidths)
        var paymentInfoTable = Table(paymentColumnWidths)

        // read clientSelectedData array in invoice if exists
        val clientSelectedData = invoice.optJSONArray("clientSelectedData") ?: JSONArray()

        // add "INVOICE TO" paragraph
        val invoiceToParagraph = Paragraph("invoice to:")
            .setFont(bebasFont)
            .setFontSize(9f)
            .setFontColor(black)
        document.add(invoiceToParagraph.setFixedPosition(50f, 650f, 100f))

        // add client name paragraph
        val clientName = if (clientSelectedData.length() > 0) {
            clientSelectedData.getJSONObject(0).getString("name")
        } else {
            ""
        }

        if (clientName != "") {
            val clientNameParagraph = Paragraph(clientName)
                .setFont(bebasFont)
                .setFontSize(18f)
                .setFontColor(blue)
            document.add(clientNameParagraph.setFixedPosition(50f, 627f, 450f))
        } else {
            val clientNameParagraph = Paragraph("Please select a client")
                .setFont(bebasFont)
                .setFontSize(18f)
                .setFontColor(gray)
            document.add(clientNameParagraph.setFixedPosition(50f, 627f, 450f))
        }

        // add address paragraph
        val address = if (clientSelectedData.length() > 0) {
            clientSelectedData.getJSONObject(0).getString("address")
        } else {
            ""
        }

        var clientInfoParagraph = Paragraph()
            .setFont(bebasFont)
            .setFontSize(9f)
            .setMultipliedLeading(-0.6f)

        if (address != "") {
            val addressParagraph = Paragraph("address: ")
                .setFont(bebasFont)
                .setFontSize(9f)
                .setFontColor(blue)

            val addressValueParagraph = Paragraph(address)
                .setFont(bebasFont)
                .setFontSize(9f)
                .setFontColor(black)

            addressParagraph.add(addressValueParagraph)

            clientInfoParagraph.add(addressParagraph)
        } else {
            clientInfoParagraph.add(Paragraph(" "))
        }

        // add phone & email paragraph
        val phone = if (clientSelectedData.length() > 0) {
            clientSelectedData.getJSONObject(0).getString("phone")
        } else {
            ""
        }
        val email = if (clientSelectedData.length() > 0) {
            clientSelectedData.getJSONObject(0).getString("email")
        } else {
            ""
        }

        if (phone != "") {
            val phoneParagraph = Paragraph("phone: ")
                .setFont(bebasFont)
                .setFontSize(9f)
                .setFontColor(blue)

            val phoneValueParagraph = Paragraph(phone)
                .setFont(bebasFont)
                .setFontSize(9f)
                .setFontColor(black)

            phoneParagraph.add(phoneValueParagraph)

            if (email != "") {
                val emailParagraph = Paragraph("email: ")
                    .setFont(bebasFont)
                    .setFontSize(9f)
                    .setFontColor(blue)

                val emailValueParagraph = Paragraph(email)
                    .setFont(bebasFont)
                    .setFontSize(9f)
                    .setFontColor(black)

                phoneParagraph.add(" ").add(" ").add(" ").add(" ").add(" ").add(" ").add(emailParagraph).add(emailValueParagraph)
            }

            clientInfoParagraph.add("\n").add(phoneParagraph).setMultipliedLeading(-0.4f)
        } else {
            clientInfoParagraph.add("\n").add(Paragraph(" ")).setMultipliedLeading(-0.4f)
        }

        clientInfoTable.addCell(Cell(1, 1)
            .setBorder(SolidBorder(white, 1.3f))
            .setBorderLeft(SolidBorder(white, 0f))
            .add(clientInfoParagraph)
            .setPaddings(-1.5f, 0f, 7f, 0f)
            .setKeepTogether(true))

        // add invoice number date paragraph
        var invoiceNumberParagraph = Paragraph("invoice no:")
            .setFont(bebasFont)
            .setFontSize(9f)
            .setFontColor(blue)
            .setMultipliedLeading(0.7f)

        val invoiceNumberValueParagraph = Paragraph(invoice.getString("invoiceNumber"))
            .setFont(bebasFont)
            .setFontSize(9f)
            .setFontColor(black)
            .setMultipliedLeading(0.7f)

        invoiceNumberParagraph.add("\n").add(invoiceNumberValueParagraph).setMultipliedLeading(0.7f)

        clientInfoTable.addCell(Cell(1, 2)
            .setBorder(SolidBorder(white, 1.3f))
            .add(invoiceNumberParagraph)
            .setPaddings(0f, 0f, 7f, 0f)
            .setKeepTogether(true))

        var invoiceDateParagraph = Paragraph("invoice date:")
            .setFont(bebasFont)
            .setFontSize(9f)
            .setFontColor(blue)
            .setMultipliedLeading(0.7f)

        var invoiceDateValue = invoice.getString("invoiceDate")
        // convert date format (ex: "06\/02\/2024" to 06 February 2024)
        val dateParts = invoiceDateValue.split("/")
        val month = when (dateParts[1]) {
            "01" -> "January"
            "02" -> "February"
            "03" -> "March"
            "04" -> "April"
            "05" -> "May"
            "06" -> "June"
            "07" -> "July"
            "08" -> "August"
            "09" -> "September"
            "10" -> "October"
            "11" -> "November"
            "12" -> "December"
            else -> ""
        }
        invoiceDateValue = "${dateParts[0]} $month ${dateParts[2]}"
        val invoiceDateValueParagraph = Paragraph(invoiceDateValue)
            .setFont(bebasFont)
            .setFontSize(9f)
            .setFontColor(black)
            .setMultipliedLeading(0.7f)

        invoiceDateParagraph.add("\n").add(invoiceDateValueParagraph).setMultipliedLeading(0.7f)

        clientInfoTable.addCell(Cell(1, 1)
            .setBorder(SolidBorder(white, 1.3f))
            .add(invoiceDateParagraph)
            .setPaddings(0f, 1.3f, 7f, 0f)
            .setKeepTogether(true))

        // ============================ add pdf item ============================

        // get selectedItems data if exists
        val selectedItems = invoice.optJSONArray("selectedItems") ?: JSONArray()

        // add itemListTable header
        val nameHeader = Paragraph("item description")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(white)
            .setTextAlignment(TextAlignment.JUSTIFIED)

        val priceHeader = Paragraph("price")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(white)
            .setTextAlignment(TextAlignment.CENTER)

        val quantityHeader = Paragraph("qty.")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(white)
            .setTextAlignment(TextAlignment.CENTER)

        val totalHeader = Paragraph("total")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(white)
            .setTextAlignment(TextAlignment.CENTER)

        val nameHeaderCell = Cell()
            .setPaddings(5f, 15.5f, 5f, 15.5f)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBackgroundColor(blue)
            .setFontColor(white)
            .setBorder(Border.NO_BORDER)
            .setBorderRight(SolidBorder(white, 1.3f))
            .setKeepTogether(true)
            .setBackgroundColor(blue)
            .add(nameHeader)

        val priceHeaderCell = Cell()
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBackgroundColor(blue)
            .setFontColor(white)
            .setBorder(Border.NO_BORDER)
            .setBorderRight(SolidBorder(white, 1.3f))
            .setBackgroundColor(blue)
            .add(priceHeader)

        val quantityHeaderCell = Cell()
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBackgroundColor(blue)
            .setFontColor(white)
            .setBorder(Border.NO_BORDER)
            .setBorderRight(SolidBorder(white, 1.3f))
            .setBackgroundColor(blue)
            .add(quantityHeader)

        val totalHeaderCell = Cell()
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBackgroundColor(blue)
            .setFontColor(white)
            .setBorder(Border.NO_BORDER)
            .setBorderRight(SolidBorder(white, 1.3f))
            .setBackgroundColor(blue)
            .add(totalHeader)

        itemListTable.addHeaderCell(nameHeaderCell)
        itemListTable.addHeaderCell(priceHeaderCell)
        itemListTable.addHeaderCell(quantityHeaderCell)
        itemListTable.addHeaderCell(totalHeaderCell)

        var isHeaderAdded = false
        var previousPageItemListTableHeight = 0f

        // read id and quantity from selectedItems
        for (i in 0 until selectedItems.length()) {
            val item = selectedItems.getJSONObject(i)
            val id = item.getString("id")
            val quantity = item.getString("quantity")

            // read ItemList.json by id, get name, description, unitCost
            val itemListFile = File(filesDir, "ItemList.json")
            val itemListArray = JSONArray(itemListFile.readText())
            var name = ""
            var description = ""
            var unitCost = ""
            for (j in 0 until itemListArray.length()) {
                val itemObject = itemListArray.getJSONObject(j)
                if (itemObject.getString("id") == id) {
                    name = itemObject.getString("name")
                    description = itemObject.getString("description")
                    unitCost = itemObject.getString("unitCost")
                    break
                }
            }

            var total = (unitCost.toDouble() * quantity.toDouble()).toString()

            // name paragraph
            val nameParagraph = Paragraph(name)
                .setFont(bebasFont)
                .setFontSize(13f)
                .setFontColor(black)
                .setTextAlignment(TextAlignment.JUSTIFIED)

            // if description is not ""
            if (description != "") {
                val descriptionParagraph = Paragraph(description)
                    .setFont(bebasFont)
                    .setFontSize(10f)
                    .setFontColor(gray)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                nameParagraph.add("\n").add(descriptionParagraph)
            }

            // set unit cost
            unitCost = setDouleValueBasedOnSettingsToString(unitCost.toDouble(), true)

            // unit cost paragraph
            val unitCostParagraph = Paragraph("$unitCost")
                .setFont(bebasFont)
                .setFontSize(13f)
                .setFontColor(black)
                .setTextAlignment(TextAlignment.CENTER)

            // quantity paragraph
            val quantityParagraph = Paragraph(quantity)
                .setFont(bebasFont)
                .setFontSize(13f)
                .setFontColor(black)
                .setTextAlignment(TextAlignment.CENTER)

            // set total text
            total = setDouleValueBasedOnSettingsToString(total.toDouble(), true)

            // total paragraph
            val totalParagraph = Paragraph("$total")
                .setFont(bebasFont)
                .setFontSize(13f)
                .setFontColor(black)
                .setTextAlignment(TextAlignment.CENTER)

            val nameCell = Cell()
                .add(nameParagraph)
                .setPaddings(5f, 15.5f, 5f, 15.5f)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderBottom(SolidBorder(DeviceRgb(235, 235, 235), 1f))
                .setBorderRight(SolidBorder(DeviceRgb(191, 191, 191), 1.3f))
                .setKeepTogether(true)

            val unitCostCell = Cell()
                .add(unitCostParagraph)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorderTop(Border.NO_BORDER)
                .setBorderBottom(SolidBorder(DeviceRgb(235, 235, 235), 1f))
                .setBorderRight(SolidBorder(DeviceRgb(191, 191, 191), 1.3f))

            val quantityCell = Cell()
                .add(quantityParagraph)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorderTop(Border.NO_BORDER)
                .setBorderBottom(SolidBorder(DeviceRgb(235, 235, 235), 1f))
                .setBorderRight(SolidBorder(DeviceRgb(191, 191, 191), 1.3f))

            val totalCell = Cell()
                .add(totalParagraph)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorderTop(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderBottom(SolidBorder(DeviceRgb(235, 235, 235), 1f))

            // add to itemListTable
            itemListTable.addCell(nameCell)
            itemListTable.addCell(unitCostCell)
            itemListTable.addCell(quantityCell)
            itemListTable.addCell(totalCell)
        }

        // ============================ add payment info and statistics ============================

        val divPaymentAndNote = Paragraph()
            .setMultipliedLeading(0.6f)

        // read selectedPaymentInstructions array in invoice if exists
        val selectedPaymentInstructions = invoice.optJSONArray("selectedPaymentInstructions") ?: JSONArray()

        val paymentInstructionListFile = File(filesDir, "PaymentInstruction.json")
        val paymentInstructionListArray = JSONArray(paymentInstructionListFile.readText())

        var tempName = ""

        // if selectedPaymentInstructions is not null and not empty
        if (selectedPaymentInstructions.length() > 0) {

            val paymentInfoParagraph = Paragraph("payment info:")
                .setFont(bebasFont)
                .setFontSize(13f)
                .setFontColor(black)
                .setTextAlignment(TextAlignment.LEFT)

            divPaymentAndNote
                .add(paymentInfoParagraph)

            for (i in 0 until selectedPaymentInstructions.length()) {

                val paymentInstruction = selectedPaymentInstructions.getString(i)

                // search paymentInstruction in PaymentInstructionList.json by id
                for (j in 0 until paymentInstructionListArray.length()) {

                    val paymentInstructionObject = paymentInstructionListArray.getJSONObject(j)

                    if (paymentInstructionObject.getString("id") == paymentInstruction) {

                        if (tempName != paymentInstructionObject.getString("payableTo")) {
                            val paymentNameParagraph = Paragraph()
                                .setFont(bebasFont)
                                .setFontSize(10f)
                                .setFontColor(gray)
                                .setTextAlignment(TextAlignment.JUSTIFIED)

                            paymentNameParagraph
                                .add("a/c name: ")
                                .add(paymentInstructionObject.getString("payableTo"))

                            divPaymentAndNote
                                .add("\n")
                                .add(paymentNameParagraph)
                                .add("\n")

                            tempName = paymentInstructionObject.getString("payableTo")
                        }

                        val paymentDetailParagraph = Paragraph()
                            .setFont(bebasFont)
                            .setFontSize(10f)
                            .setFontColor(gray)
                            .setTextAlignment(TextAlignment.JUSTIFIED)

                        paymentDetailParagraph
                            .add(paymentInstructionObject.getString("method"))
                            .add(": ")
                            .add(paymentInstructionObject.getString("paymentDetail"))

                        divPaymentAndNote
                            .add(paymentDetailParagraph)
                            .add("\n")
                    }
                }
            }
        }

        val termsAndConditionsParagraph = Paragraph("\n" + "terms & conditions/notes:")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setTextAlignment(TextAlignment.LEFT)

        // read notes from json
        var notes = invoice.optString("note", "")

        val termsAndConditionsValueParagraph = Paragraph(notes)
            .setFont(bebasFont)
            .setMultipliedLeading(0.9f)
            .setFontSize(10f)
            .setFontColor(gray)
            .setTextAlignment(TextAlignment.JUSTIFIED)

        if (notes != "") {
            divPaymentAndNote
                .add(termsAndConditionsParagraph)
                .add("\n")
                .add(termsAndConditionsValueParagraph)
        }

        paymentInfoTable.addCell(Cell(3, 2)
            .add(divPaymentAndNote)
            .setPaddings(8f, 29f, 5f, 0f)
            .setBorder(Border.NO_BORDER)
            .setKeepTogether(true))

        // value
        var subTotalValue = invoice.optString("subtotal", "0")
        subTotalValue = setDouleValueBasedOnSettingsToString(subTotalValue.toDouble(), true)
        val subTotalValueParagraph = Paragraph("$subTotalValue")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setMultipliedLeading(0.9f)
            .setTextAlignment(TextAlignment.CENTER)

        var discountValue = invoice.optString("discount", "0")
        discountValue = setDouleValueBasedOnSettingsToString(discountValue.toDouble(), true)
        val discountValueParagraph = Paragraph("$discountValue")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setMultipliedLeading(0.9f)
            .setTextAlignment(TextAlignment.CENTER)

        var shippingValue = invoice.optString("shipping", "0")
        shippingValue = setDouleValueBasedOnSettingsToString(shippingValue.toDouble(), true)
        val shippingValueParagraph = Paragraph("$shippingValue")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setMultipliedLeading(0.9f)
            .setTextAlignment(TextAlignment.CENTER)

        var taxValue = invoice.optString("tax", "0")
        taxValue = setDouleValueBasedOnSettingsToString(taxValue.toDouble(), true)
        val taxValueParagraph = Paragraph("$taxValue")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setMultipliedLeading(0.9f)
            .setTextAlignment(TextAlignment.CENTER)

        var totalValue = invoice.optString("total", "0")
        totalValue = setDouleValueBasedOnSettingsToString(totalValue.toDouble(), true)
        val totalValueParagraph = Paragraph("$totalValue")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setMultipliedLeading(0.9f)
            .setTextAlignment(TextAlignment.CENTER)

        // read amount from paymentList array
        val paymentList = invoice.optJSONArray("paymentList") ?: JSONArray()
        var amountPaid = 0f
        for (i in 0 until paymentList.length()) {
            val payment = paymentList.getJSONObject(i)
            amountPaid += payment.getString("amount").toFloat()
        }
        val amountPaisString = setDouleValueBasedOnSettingsToString(amountPaid.toDouble(), true)

        val amountPaidValueParagraph = Paragraph("$amountPaisString")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setMultipliedLeading(0.9f)
            .setTextAlignment(TextAlignment.CENTER)

        var balanceDueValue = invoice.optString("balanceDue", "0")
        balanceDueValue = setDouleValueBasedOnSettingsToString(balanceDueValue.toDouble(), true)
        val balanceDueValueParagraph = Paragraph("$balanceDueValue")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(white)
            .setBackgroundColor(blue)
            .setMultipliedLeading(0.9f)
            .setTextAlignment(TextAlignment.CENTER)

        val subTotalParagraph = Paragraph("sub total:")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMultipliedLeading(0.9f)

        // cal percent discount
        /*val percentDiscount = (discountValue.toFloat() / subTotalValue.toFloat() * 100)
        val percentDiscountString = String.format("%.2f", percentDiscount)*/

        val discountParagraph = Paragraph("discount:")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMultipliedLeading(0.9f)

        val shippingParagraph = Paragraph("shipping:")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMultipliedLeading(0.9f)

        val taxParagraph = Paragraph("tax:")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMultipliedLeading(0.9f)

        val totalParagraph = Paragraph("total:")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMultipliedLeading(0.9f)

        val amountPaidParagraph = Paragraph("payment:")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMultipliedLeading(0.9f)

        val balanceDueParagraph = Paragraph("amount due:")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(white)
            .setTextAlignment(TextAlignment.RIGHT)
            .setBackgroundColor(blue)
            .setMultipliedLeading(0.9f)

        val divStatistics = Div()

        if (invoice.optString("status") == "paid") {

            val drawable = resources.getDrawable(resources.getIdentifier("pdf_paid", "drawable", packageName), theme)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val bitmapData = stream.toByteArray()

            // Create image data from bitmap data
            val paidImageData = ImageDataFactory.create(bitmapData)

            // Create image from image data
            val paidImage = Image(paidImageData)

            paidImage
                .scaleToFit(92f, 55f)
                .setMarginRight(-240f)
                .setMarginBottom(-110f)

            divStatistics
                .add(paidImage)
                .add(subTotalParagraph)
                .add(discountParagraph)
                .add(shippingParagraph)
                .add(taxParagraph)
                .add(totalParagraph)
                .add(amountPaidParagraph)
                .setMarginTop(55f)
                .setMarginBottom(-55f)
                .setMarginLeft(65f)

        } else {
            divStatistics
                .add(subTotalParagraph)
                .add(discountParagraph)
                .add(shippingParagraph)
                .add(taxParagraph)
                .add(totalParagraph)
                .add(amountPaidParagraph)
        }

        val divStatisticsValue = Div()
            .add(subTotalValueParagraph)
            .add(discountValueParagraph)
            .add(shippingValueParagraph)
            .add(taxValueParagraph)
            .add(totalValueParagraph)
            .add(amountPaidValueParagraph)

        paymentInfoTable.addCell(Cell(1, 1)
            .add(divStatistics)
            .setPaddings(5f, 0f, 0f, 0f)
            .setBorder(Border.NO_BORDER)
            .setKeepTogether(true))

        paymentInfoTable.addCell(Cell(1, 1)
            .add(divStatisticsValue)
            .setPaddings(5f, 0f, 0f, 0f)
            .setBorder(Border.NO_BORDER)
            .setKeepTogether(true))

        paymentInfoTable.addCell(Cell(1, 1)
            .add(balanceDueParagraph)
            .setBackgroundColor(blue)
            .setPaddings(5f, 0f, 7f, 0f)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBorder(Border.NO_BORDER))

        paymentInfoTable.addCell(Cell(1, 1)
            .add(balanceDueValueParagraph)
            .setBackgroundColor(blue)
            .setPaddings(5f, 0f, 7f, 0f)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBorder(Border.NO_BORDER))
            .setBorderRight(SolidBorder(white, 1.3f))

        // buyer signature

        val divBuyerSignature = Div()
        var buyerSignaturePath = invoice.optString("buyerSignature", null)
        // if exists, add it to div
        if (buyerSignaturePath != null && buyerSignaturePath != "") {
            // if has not "/data/user/0/ai.girlfriend.app/files/", add it to start
            if (!buyerSignaturePath.contains("data/user/0/ai.girlfriend.app/files/", ignoreCase = false)) {
                buyerSignaturePath = "/data/user/0/ai.girlfriend.app/files/$buyerSignaturePath"
            }
            val buyerSignature = Image(ImageDataFactory.create(buyerSignaturePath))
            buyerSignature
                .scaleToFit(69f, 69f)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
            divBuyerSignature.add(buyerSignature)

            // add divider line to div
            val drawable = resources.getDrawable(resources.getIdentifier("signature_divider_line", "drawable", packageName), theme)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val bitmapData = stream.toByteArray()

            // Create image data from bitmap data
            val signatureDividerLineImageData = ImageDataFactory.create(bitmapData)

            // Create image from image data
            val signatureDividerLineImage = Image(signatureDividerLineImageData)

            // scale image to fit
            signatureDividerLineImage
                .scaleToFit(59f, 59f)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)

            // add to div
            divBuyerSignature.add(signatureDividerLineImage)

            // seller signature paragraph
            val buyerSignatureParagraph = Paragraph("buyer signature")
                .setFont(bebasFont)
                .setFontSize(10.5f)
                .setFontColor(black)
                .setMultipliedLeading(0.8f)
                .setTextAlignment(TextAlignment.CENTER)

            // add seller signature paragraph to center of div
            divBuyerSignature.add(buyerSignatureParagraph)

            paymentInfoTable.addCell(Cell(1, 1)
                .add(divBuyerSignature)
                .setBorder(Border.NO_BORDER)
                .setPaddings(1f, 0f, 0f, 20f)
                .setKeepTogether(true))
        } else {
            paymentInfoTable.addCell(Cell(1, 1)
                .add(Div())
                .setBorder(Border.NO_BORDER))
        }

        // seller signature

        val divSellerSignature = Div()
        var sellerSignaturePath = invoice.optString("sellerSignature", null)
        // if exists, add it to div
        if (sellerSignaturePath != null && sellerSignaturePath != "") {
            // if has not "/data/user/0/ai.girlfriend.app/files/", add it to start
            if (!sellerSignaturePath.contains("data/user/0/ai.girlfriend.app/files/", ignoreCase = false)) {
                sellerSignaturePath = "/data/user/0/ai.girlfriend.app/files/$sellerSignaturePath"
            }
            val sellerSignature = Image(ImageDataFactory.create(sellerSignaturePath))
            sellerSignature
                .scaleToFit(69f, 69f)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
            divSellerSignature.add(sellerSignature)

            // add divider line to div
            val drawable = resources.getDrawable(resources.getIdentifier("signature_divider_line", "drawable", packageName), theme)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val bitmapData = stream.toByteArray()

            // Create image data from bitmap data
            val signatureDividerLineImageData = ImageDataFactory.create(bitmapData)

            // Create image from image data
            val signatureDividerLineImage = Image(signatureDividerLineImageData)

            // scale image to fit
            signatureDividerLineImage
                .scaleToFit(59f, 59f)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)

            // add to div
            divSellerSignature.add(signatureDividerLineImage)

            // seller signature paragraph
            val sellerSignatureParagraph = Paragraph("seller signature")
                .setFont(bebasFont)
                .setFontSize(10.5f)
                .setFontColor(black)
                .setMultipliedLeading(0.8f)
                .setTextAlignment(TextAlignment.CENTER)

            // add seller signature paragraph to center of div
            divSellerSignature.add(sellerSignatureParagraph)

            paymentInfoTable.addCell(Cell(1, 1)
                .add(divSellerSignature)
                .setPaddings(1f, 0f, 0f, 8f)
                .setBorder(Border.NO_BORDER)
                .setKeepTogether(true))
        } else {
            paymentInfoTable.addCell(Cell(1, 1)
                .add(Div())
                .setBorder(Border.NO_BORDER))
        }

        clientInfoTable
            .setMargins(40f, 12.6f, -5f, 13.8f)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
        paymentInfoTable
            .setMargins(-5f, 12.6f, 10f, 13.8f)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setKeepTogether(true)
        itemListTable
            .setMargins(0f, 12.6f, 10f, 13.8f)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)

        // add table to document
        document.add(clientInfoTable)
        document.add(itemListTable)
        document.add(paymentInfoTable)

        var numberOfPages = document.pdfDocument.numberOfPages

        // ============================ footer ============================

        val thankYouParagraph = Paragraph("thank you for your business")
            .setFont(bebasFont)
            .setFontSize(13f)
            .setFontColor(black)
            .setTextAlignment(TextAlignment.LEFT)
            .setFixedPosition(numberOfPages, 50f, 60f, 450f)
        document.add(thankYouParagraph)

        addImageToDocument(document, "thank_you_footer_divider_line", 50f, 59f, 196f, PageSize.A4.height, null)

        // if has business address
        val businessAddress = businessData.optString("businessAddress", null)
        if (businessAddress != null) {
            val businessAddressParagraph = Paragraph(businessAddress)
                .setFont(bebasFont)
                .setFontSize(10f)
                .setFontColor(black)
                .setTextAlignment(TextAlignment.LEFT)
                .setFixedPosition(numberOfPages, 50f, 40f, 450f)
            document.add(businessAddressParagraph)
        }

        // phone
        addImageToDocument(document, "phone_icon", 50f, 30f, 7f, PageSize.A4.height, null)
        val businessPhone = businessData.optString("businessPhoneNumber", null)
        if (businessPhone != null) {
            val businessPhoneParagraph = Paragraph(businessPhone)
                .setFont(bebasFont)
                .setFontSize(8f)
                .setFontColor(black)
                .setTextAlignment(TextAlignment.LEFT)
                .setFixedPosition(numberOfPages, 60f, 26.5f, 450f)
            document.add(businessPhoneParagraph)
        }

        val businessEmail = businessData.optString("businessEmail", null)
        // email
        if (businessEmail != null) {
            addImageToDocument(document, "email_icon", 100f, 30f, 7f, PageSize.A4.height, null)
            val businessEmailParagraph = Paragraph(businessEmail)
                .setFont(bebasFont)
                .setFontSize(8f)
                .setFontColor(black)
                .setTextAlignment(TextAlignment.LEFT)
                .setFixedPosition(numberOfPages, 110f, 26.5f, 450f)
            document.add(businessEmailParagraph)
        }

        // photo attach =========================================================================

        // read array of photosAttach
        val photosAttach = invoice.optJSONArray("photosAttach") ?: JSONArray()

        // if has not "/data/user/0/ai.girlfriend.app/files/", add it to start
        for (i in 0 until photosAttach.length()) {
            val photoPath = photosAttach.getString(i)
            if (!photoPath.contains("data/user/0/ai.girlfriend.app/files/", ignoreCase = false)) {
                photosAttach.put(i, "/data/user/0/ai.girlfriend.app/files/$photoPath")
            }
        }

        // calculate the number of pages needed for photosAttach (4 photos per page)
        val photosAttachPages = ceil(photosAttach.length().toDouble() / 4).toInt()

        // add new page for photosAttach to document
        for (i in 0..<(photosAttachPages + numberOfPages)) {
            // add header image to top from drawable
            addImageToDocument(document, "pdf_header", 0f, 689f, PageSize.A4.width, PageSize.A4.height, i + 1)

            // add logo and business name to top right of header
            val businessName = businessData.getString("businessName")
            val businessLogoPath = businessData.optString("businessLogo", null)

            // add business name to top right of header
            val businessNameParagraph = Paragraph(businessName)
                .setFont(bebasFont)
                .setFontSize(15f)
                .setFontColor(white)
                .setMultipliedLeading(0.8f)

            // if has business email, add it to business name paragraph
            if (businessEmail != null) {
                val businessEmailParagraph = Paragraph(businessEmail)
                    .setFont(bebasFont)
                    .setFontSize(10f)
                    .setFontColor(white)
                    .setMultipliedLeading(0.8f)
                businessNameParagraph.add("\n").add(businessEmailParagraph)
            }

            val divBusinessName = Div()
                .setHeight(100f)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(businessNameParagraph)

            document.add(divBusinessName.setFixedPosition(i + 1, PageSize.A4.width - 133f, PageSize.A4.height - 122f, 109f))

            // if business logo exists, add it to left of business name
            if (businessLogoPath != null) {
                val businessLogo = Image(ImageDataFactory.create(businessLogoPath))
                businessLogo.scaleToFit(45f, 45f)
                businessLogo.setFixedPosition(i + 1, PageSize.A4.width - 190f, PageSize.A4.height - 95f)
                document.add(businessLogo)
            }

            // add header divider to bottom of header
            addImageToDocument(document, "pdf_header_divider", 49f, 684f, PageSize.A4.width, PageSize.A4.height, i + 1)

            if (i >= numberOfPages) {
                // add photosAttach to document (2x2 table)
                val columnWidths = floatArrayOf(300f, 280f)
                val table = Table(columnWidths)

                // add photosAttach to table
                for (j in 0 until 4) {
                    val photoIndex = (i - numberOfPages) * 4 + j
                    if (photoIndex < photosAttach.length()) {
                        val photoPath = photosAttach.getString(photoIndex)
                        val photo = Image(ImageDataFactory.create(photoPath))
                        photo.scaleToFit(230f, 230f)
                        table.addCell(Cell()
                            .setPaddings(0f, 40f, 45f, 0f)
                            .add(photo)
                            .setBorder(Border.NO_BORDER))
                    } else {
                        table.addCell(Cell().setBorder(Border.NO_BORDER))
                    }
                }

                table
                    .setMargins(40f, 12.6f, 10f, 13.8f)

                // add table to document
                document.add(table)
            }
        }

        // close document
        document.close()

        // return pdf file
        return pdfFile
    }

    // get table height
    private fun getTableHeight(table: Table, document: Document): Float {
        val layoutResult = table
            .createRendererSubTree()
            .setParent(document.renderer)
            .layout(LayoutContext(LayoutArea(1,
                Rectangle(PageSize.A4.width, PageSize.A4.height - 180f))))
        return layoutResult.occupiedArea.bBox.height
    }

    // Add image to document
    private fun addImageToDocument(document: Document, imagePath: String, left: Float, bottom: Float, scaleWidth: Float, scaleHeight: Float, page: Int?) {
        // Convert drawable to bitmap
        val drawable = resources.getDrawable(resources.getIdentifier(imagePath, "drawable", packageName), theme)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bitmapData = stream.toByteArray()

        // Create image data from bitmap data
        val imageData = ImageDataFactory.create(bitmapData)

        // Create image from image data
        val image = Image(imageData)

        // set image width to page width, height will be scaled accordingly
        image.scaleToFit(scaleWidth, scaleHeight)

        // Set the position of the image
        if (page != null) {
            image.setFixedPosition(page, left, bottom)
        } else {
            image.setFixedPosition(left, bottom)
        }

        // Add image to the document
        document.add(image)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateBack()
    }

    // navigate back
    private fun navigateBack() {
        // remove PDF file from filesDir
        //pdfFile?.delete()

        // delete InvoicePDF folder
        deleteInvoicePDFFolder()

        setResult(RESULT_CANCELED)

        finish()
    }

    // Delete InvoicePDF folder
    private fun deleteInvoicePDFFolder() {
        val invoicePDFDir = File(filesDir, "InvoicePDF")
        if (invoicePDFDir.exists()) {
            invoicePDFDir.deleteRecursively()
        }
    }

    // Read the business data from the file
    private fun readBusinessData(): JSONObject? {
        val businessDataFile = File(filesDir, "BusinessData.json")
        return if (businessDataFile.exists()) {
            val jsonData = businessDataFile.readText()
            JSONObject(jsonData)
        } else null
    }

    // Read the temp invoice data from the file
    private fun readTempInvoiceData(): JSONObject? {
        val tempFile = File(filesDir, "tempInvoiceData.json")
        return if (tempFile.exists()) {
            val jsonData = tempFile.readText()
            JSONObject(jsonData)
        } else null
    }

    // Read invoice in InvoiceList.json
    private fun readInvoice(invoiceId: String): JSONObject? {
        val invoiceListFile = File(filesDir, "InvoiceList.json")

        var invoice: JSONObject? = null
        if (invoiceListFile.exists()) {
            val invoiceListArray = JSONArray(invoiceListFile.readText())
            for (i in 0 until invoiceListArray.length()) {
                val invoiceObject = invoiceListArray.getJSONObject(i)
                if (invoiceObject.getString("id") == invoiceId) {
                    invoice = invoiceObject
                    break
                }
            }
        }

        return invoice
    }

    private fun readSettings(): JSONObject {
        val file = File(filesDir, "Settings.json")
        return if (file.exists()) {
            JSONObject(file.readText())
        } else {
            JSONObject()
        }
    }

    private fun setStringDateBasedOnSettings(date: String, isConvertTo_ddMMyyyy: Boolean): String {
        if (isConvertTo_ddMMyyyy) {
            // convert to dd/MM/yyyy
            val dateFormat = SimpleDateFormat(dateFormatSetting, Locale.getDefault())
            val dateObj = dateFormat.parse(date)
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateObj)
        } else {
            // convert base on date format setting
            val dateFormat = SimpleDateFormat(dateFormatSetting, Locale.getDefault())
            val dateObj = dateFormat.parse(date)
            return SimpleDateFormat(dateFormatSetting, Locale.getDefault()).format(dateObj)
        }
    }

    private fun setDouleValueBasedOnSettingsToString(value: Double, isInculdeSymbol: Boolean): String {

        var formattedNumber = ""

        if (value.isNaN()) {
            formattedNumber = "0"
        }

        if (value != 0.0) {

            // set the locale based on the settings
            val decimalFormatSymbols = DecimalFormatSymbols().apply {
                decimalSeparator = if (numberFormat == 3 || numberFormat == 4) ',' else '.'
                groupingSeparator = when (numberFormat) {
                    1, 3 -> ' '  // 1, 3 use space
                    4 -> '.'      // 4 use dot
                    else -> ','  // default (2) use comma
                }
            }

            // format the number based on decimal places
            val numberFormatPattern = "###,###.${"0".repeat(decimalPlaces)}"
            val numberFormatter = DecimalFormat(numberFormatPattern, decimalFormatSymbols)
            val formatted = numberFormatter.format(value)

            if (isInculdeSymbol) {
                // format the number based on currency position
                formattedNumber = if (currencyPosition == "before") {
                    "$currencySymbol$formatted"
                } else {
                    "$formatted$currencySymbol"
                }
            } else {
                formattedNumber = formatted
            }

            // if decimalPlaces is 0, remove the decimal point
            if (decimalPlaces == 0) {
                if (numberFormat == 1 || numberFormat == 2) {
                    formattedNumber = formattedNumber.replace(".", "")
                } else {
                    formattedNumber = formattedNumber.replace(",", "")
                }
            }

        } else {
            if (isInculdeSymbol) {
                // format the number based on currency position
                formattedNumber = if (currencyPosition == "before") {
                    currencySymbol + "0"
                } else {
                    "0$currencySymbol"
                }
            } else {
                formattedNumber = "0"
            }
        }

        when (numberFormat) {
            1, 2 -> {
                // if first char is dot, add 0 before dot
                if (formattedNumber.startsWith(".")) {
                    formattedNumber = "0$formattedNumber"
                }
            }
            3, 4 -> {
                // if first char is comma, add 0 before comma
                if (formattedNumber.startsWith(",")) {
                    formattedNumber = "0$formattedNumber"
                }
            }
        }

        if (currencySymbol != "$") {
            // replace currency symbol to $
            formattedNumber = formattedNumber.replace(currencySymbol, "$")
        }

        return formattedNumber
    }

    private fun convertStringTextViewToDouble(value: String): Double {

        if (value.isNullOrEmpty()) {
            return 0.0
        }

        var formattedValue: String

        // remove currency symbol, remove space
        formattedValue = value.replace(currencySymbol, "").replace(" ", "")

        when (numberFormat) {
            2 -> { // 10,000.00
                // replace comma with dot
                formattedValue = formattedValue.replace(",", "")
            }
            3 -> { // 10 000,00
                // replace space with nothing, then replace comma with dot
                formattedValue = formattedValue.replace(",", ".")
            }
            4 -> { // 10.000,00
                // replace dot with nothing, then replace comma with dot
                formattedValue = formattedValue.replace(".", "").replace(",", ".")
            }
        }

        return formattedValue.toDouble()
    }
}