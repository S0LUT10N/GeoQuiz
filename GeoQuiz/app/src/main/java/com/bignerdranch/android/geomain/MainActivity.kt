package com.bignerdranch.android.geomain

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

        private lateinit var trueButton: Button
        private lateinit var falseButton: Button
        private lateinit var nextButton: ImageButton
        private lateinit var backButton: ImageButton
        private lateinit var cheatButton: Button
        val IntList = mutableListOf<Int>()
        private lateinit var questionTextView: TextView

        private val quizViewModel: QuizViewModel by lazy {
            ViewModelProviders.of(this).get(QuizViewModel::class.java)
        }
        @SuppressLint("RestrictedApi")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Log.d(TAG, "onCreate(Bundle?) called")
            setContentView(R.layout.activity_main)

            val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
            quizViewModel.currentIndex = currentIndex
            trueButton = findViewById(R.id.true_button)
            falseButton = findViewById(R.id.false_button)
            nextButton = findViewById(R.id.next_button)
            backButton = findViewById(R.id.back_button)
            cheatButton = findViewById(R.id.cheat_button)
            questionTextView = findViewById(R.id.question_text_view)
            nextButton.isEnabled = false
            backButton.isEnabled = false

            val versionTextView: TextView = findViewById(R.id.versionTextView)
            val version: String = Build.VERSION.RELEASE
            versionTextView.text = "Версия операционной системы: $version"

            trueButton.setOnClickListener { view: View ->
                checkAnswer(true)
                checkLastAnswer()
                trueButton.isEnabled = false
                falseButton.isEnabled = false
                nextButton.isEnabled = true
                backButton.isEnabled = false
            }
            falseButton.setOnClickListener { view: View ->
                checkAnswer(false)
                checkLastAnswer()
                falseButton.isEnabled = false
                trueButton.isEnabled = false
                nextButton.isEnabled = true
                backButton.isEnabled = false
            }
            nextButton.setOnClickListener {
                quizViewModel.moveToNext()
                updateQuestion()
                trueButton.isEnabled = true
                falseButton.isEnabled = true
                backButton.isEnabled = true
                nextButton.isEnabled = false
                quizViewModel.isCheater = false;
                if (quizViewModel.currentIndex == 0){
                    backButton.isEnabled = false
                }
            }
            questionTextView.setOnClickListener {
                if (nextButton.isEnabled) {
                    nextButton.performClick()
                }
            }
            backButton.setOnClickListener{
                quizViewModel.moveToBack()
                updateQuestion()
                IntList.removeLast()
                trueButton.isEnabled = true
                falseButton.isEnabled = true
                nextButton.isEnabled = false
                if (quizViewModel.currentIndex == 0){
                    backButton.isEnabled = false
                }
            }
            cheatButton.setOnClickListener { view ->
                quizViewModel.checkAnswerCheat()
                val answerIsTrue = quizViewModel.currentQuestionAnswer
                val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val options = ActivityOptions
                        .makeClipRevealAnimation(view, 0, 0, view.width, view.height)
                    startActivityForResult(intent, REQUEST_CODE_CHEAT)
                } else {
                    startActivityForResult(intent, REQUEST_CODE_CHEAT)
                }
                if(IntList.count { it == 2 } == 0){
                    val myTextView = findViewById<TextView>(R.id.myTextView)
                    myTextView.setText("Количество попыток: 2")
                }
                if(IntList.count { it == 2 } == 1){
                    val myTextView = findViewById<TextView>(R.id.myTextView)
                    myTextView.setText("Количество попыток: 1")
                }
                if(IntList.count { it == 2 } == 2){
                    cheatButton.isEnabled = false
                    val myTextView = findViewById<TextView>(R.id.myTextView)
                    myTextView.setText("Количество попыток: 0")
                }
            }
            updateQuestion()
        }

        override fun onActivityResult(requestCode: Int,
                                      resultCode: Int,
                                      data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            if (requestCode == REQUEST_CODE_CHEAT) {
                quizViewModel.isCheater =
                    data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
            }
        }

        override fun onStart() {
            super.onStart()
            Log.d(TAG, "onStart() called")
        }
        override fun onResume() {
            super.onResume()
            Log.d(TAG, "onResume() called")
        }
        override fun onPause() {
            super.onPause()
            Log.d(TAG, "onPause() called")
        }
        override fun onSaveInstanceState(savedInstanceState: Bundle) {
            super.onSaveInstanceState(savedInstanceState)
            Log.i(TAG, "onSaveInstanceState")
            savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
        }
        override fun onStop() {
            super.onStop()
            Log.d(TAG, "onStop() called")
        }
        override fun onDestroy() {
            super.onDestroy()
            Log.d(TAG, "onDestroy() called")
        }

        private fun updateQuestion() {
            val questionTextResId = quizViewModel.currentQuestionText
            questionTextView.setText(questionTextResId)
        }
        private fun checkAnswer(userAnswer: Boolean) {
            val correctAnswer = quizViewModel.currentQuestionAnswer
            val messageResId = if (quizViewModel.isCheater or quizViewModel.moveAnswerCheat()) {
                IntList.add(2)
                R.string.judgment_toast
            } else if (userAnswer == correctAnswer) {
                IntList.add(1)
                R.string.correct_toast
            } else {
                IntList.add(0)
                R.string.incorrect_toast
            }
            val toast = Toast.makeText(this, messageResId, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
        }
        private fun checkLastAnswer() {
            val correctCount = IntList.count { it == 1 }
            val cheatCount = IntList.count { it == 2 }
            val answerCount = IntList.size - IntList.count { it == 2 }
            val totalCount = IntList.size
            val percent = (correctCount.toDouble() / answerCount.toDouble()) * 100
            val percentInt = percent.toInt()
            if (totalCount == 6) {
                val message = "Your score: $correctCount out of $answerCount. Percentage correct: $percentInt%. Cheat count: $cheatCount"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                IntList.clear()
            }
        }
}