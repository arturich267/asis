package com.asis.virtualcompanion.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asis.virtualcompanion.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {

    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Set the privacy policy content
        binding.privacyPolicyText.text = getPrivacyPolicyContent()
    }

    private fun getPrivacyPolicyContent(): String {
        return """
            Политика конфиденциальности Asis Virtual Companion
            
            Дата вступления в силу: ${getCurrentDate()}
            
            1. Сбор информации
            
            Мы собираем следующую информацию для улучшения работы приложения:
            
            • Аудиозаписи: Ваш голос сохраняется локально для анализа эмоций и генерации ответов
            • Текстовые сообщения: Чаты сохраняются для обеспечения непрерывности диалога
            • Настройки пользователя: Предпочтения темы, голоса и другие персонализации
            • Данные об использовании: Анонимная статистика использования функций приложения
            
            2. Использование информации
            
            Собранная информация используется для:
            
            • Персонализации ответов виртуального компаньона
            • Анализа эмоций для улучшения взаимодействия
            • Обеспечения плавной работы чата и голосовых функций
            • Улучшения качества приложения через анализ использования
            
            3. Хранение данных
            
            • Все данные хранятся локально на вашем устройстве
            • Мы не передаем ваши данные третьим лицам
            • Вы можете удалить все данные в любое время через настройки
            • Временные файлы автоматически удаляются через 1 час
            
            4. Безопасность данных
            
            • Данные шифруются при хранении на устройстве
            • Доступ к микрофону запрашивается только при записи
            • Доступ к файлам ограничен необходимыми минимумом
            • Регулярные обновления безопасности
            
            5. Ваши права
            
            Вы имеете право:
            
            • Просматривать собранные данные
            • Удалять любые или все данные
            • Отключать запись голоса
            • Ограничивать обработку офлайн
            • Запрашивать копию ваших данных
            
            6. Дети
            
            Приложение не предназначено для детей младше 13 лет. 
            Мы сознательно не собираем информацию о детях.
            
            7. Изменения в политике
            
            Мы можем обновлять эту политику конфиденциальности. 
            Об изменениях вы будете уведомлены в приложении.
            
            8. Связь с нами
            
            Если у вас есть вопросы о политике конфиденциальности:
            
            Email: privacy@asis-virtual.com
            
            ---
            
            Эта политика конфиденциальности действительна с указанной выше даты 
            и применяется ко всем пользователям Asis Virtual Companion.
        """.trimIndent()
    }

    private fun getCurrentDate(): String {
        val java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        return java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}