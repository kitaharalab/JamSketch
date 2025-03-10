package jp.kthrlab.jamsketch.config

class Config {
    val general = General()
    val music = Music()
    val channels = mutableListOf<Channel>()
    val simple = Simple()
//    val ga = GA()
    val tf = TF()
}

/**
 * Overall application behavior
 */
data class General(
    var view_width: Int = 0,
    var view_height: Int = 0,
    var keyboard_width: Int = 0,
    var scalable: Boolean = false,
    var log_dir: String = "",
    var mode: String = "",
    var host: String = "",
    var port: Int = 0,
    var cursor_enhanced: Boolean = false,
    var on_drag_only: Boolean = true,
    var forced_progress: Boolean = false,
    var melody_resetting: Boolean = false,
    var octave_program: MutableMap<String, String> = mutableMapOf(),
    var show_guide: Boolean = false,
    var guide_smoothness: Int = 0,
    var show_bg_image : Boolean = false,
    var show_particles : Boolean = false,
)

/**
 * Music
 */
data class Music(
    var jamsketch_engine: String = "",
    var midfilename: String = "",
    var chordprog: Array<String> = emptyArray(),
    var num_of_measures: Int = 0,
    var num_of_reset_ahead: Int = 0,
    var division: Int = 0,
    var beats_per_measure: Int = 0,
    var initial_blank_measures: Int = 0,
    var repeat_times: Int = 0,
    var expression: Boolean = false,
    var melody_execution_span: Int = 0,
    var how_in_advance: Int = 0,
    var channel_guide: Int = 0,
    var channel_gen: Int = 0,
    var input_file_path: String = "",
)

/**
 * Channel
 */
data class Channel(
    var channel_number: Int = 0,
    var program_number: Int = 0,
    var program_name: String = "",
    var color: Color = Color(0, 0, 0, 0),
)

/**
 * Color by channel
 */
data class Color(var r: Int, var g: Int, var b: Int, var a: Int)

/**
 * JamSketchEngineSimple
 */
data class Simple(
    var model_file: String = "",
    var ent_bias: Double = 0.0,
    var w1: Double = 0.0,
    var w2: Double = 0.0,
    var w3: Double = 0.0,
    var w4: Double = 0.0,
)

/**
 * Genetic Algorithm
 */
//data class GA(
//    var calc_length: Int = 0,
//)

/**
 * JamSketchEngineTF1
 */
data class TF(
    var tf_model_dir: String = "",
    var tf_model_layer: String = "",
//    var tf_model_input_col: Int = 0,
//    var tf_model_output_col: Int = 0,
    var tf_note_num_start: Int = 0,
    var tf_note_con_col_start: Int = 0,
    var tf_rest_col: Int = 0,
    var tf_chord_col_start: Int = 0,
    var tf_num_of_melody_element: Int = 0,
)
