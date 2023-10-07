package com.tictactoe;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{

        // Getting the current session
        HttpSession currentSession = req.getSession();

        // We get the object of the playing field from the session
        Field field = extractField(currentSession);

        // we get the index of the cell on which the click occurred
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);

        // We check that the cell that was clicked on is empty.
        // Otherwise, we do nothing and send the user to the same page without changes
        // parameters in the session
        if (Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        // we put a cross in the cell that the user clicked on
        field.getField().put(index, Sign.CROSS);

        // We check whether the cross has not won after adding the last user click
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        // We get an empty cell of the field
        int emptyFieldIndex = field.getEmptyFieldIndex();

        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);

            // Check if the zero won after adding the last zero
            if (checkWin(resp, currentSession, field)) {
                return;
            }
        }
        // Если пустой ячейки нет и никто не победил - значит это ничья
        else {
            // Добавляем в сессию флаг, который сигнализирует что произошла ничья
            currentSession.setAttribute("draw", true);

            // Считаем список значков
            List<Sign> data = field.getFieldData();

            // Обновляем этот список в сессии
            currentSession.setAttribute("data", data);

            // Шлем редирект
            resp.sendRedirect("/index.jsp");
            return;
        }

        // Counting the list of icons
        List<Sign> data = field.getFieldData();

        // Updating the field object and the list of icons in the session
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        resp.sendRedirect("/index.jsp");


    }

    private int getSelectedIndex(HttpServletRequest request){
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

     // The method checks if there are no three tic-tac-toe in a row
     // Return true/false
    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            // Adding a flag that shows that someone has won
            currentSession.setAttribute("winner", winner);

            // Counting the list of icons
            List<Sign> data = field.getFieldData();

            // Updating this list in the session
            currentSession.setAttribute("data", data);

            // Helmet redirect
            response.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }

}
