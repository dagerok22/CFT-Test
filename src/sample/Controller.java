package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import java.util.HashMap;
import java.util.Map;


public class Controller {

    private static final int L_FREE = 0;
    private static final int L_KRESTIK = 1;
    private static final int L_NOLIK = 2;
    private static final int L_PLAYER = L_KRESTIK;
    private static final int L_AI = L_NOLIK;


    private boolean isFinished = false;
    private int winnerPlayer = 0;
    private boolean isGameOver = false;

    private int field[][];

    private int[][] win_pos = {{0,0}, {0,2}, {2,0}, {2,2} , {1,1}};

    @FXML
    private GridPane grid_field;
    @FXML
    private Button btn00;
    @FXML
    private Button btn01;
    @FXML
    private Button btn02;
    @FXML
    private Button btn10;
    @FXML
    private Button btn11;
    @FXML
    private Button btn12;
    @FXML
    private Button btn20;
    @FXML
    private Button btn21;
    @FXML
    private Button btn22;

    private Main main;


    public void setMainApp(Main mainApp) {
        this.main = mainApp;
    }


    private Map<Integer,Button> map = new HashMap<Integer,Button>();

    // Now add observability by wrapping it with ObservableList.
    private ObservableMap<Integer,Button> fieldButons = FXCollections.observableMap(map);


    private boolean check_game_over()
    {
        int cnt=0;
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                if (field[i][j] == 0) return false;
            }
        }
        return true;
    }

    private boolean isCenterFree(){
        if(field[1][1] == 0)
            return true;
        return false;
    }

    private boolean isPosFree(int[] pos){
        if(field[pos[0]][pos[1]] == 0)
            return true;
        return false;
    }


    private boolean check_win_pos()
    {
        int cnt=0;
        for (int i=0; i<5; i++)
            if (field[win_pos[i][0]][win_pos[i][1]]!=0) cnt++;
        if (cnt==5) return false;
        return true;
    }

    private void visualizeMove(){
        for (int i = 0; i < 9; i++) {
            Button current_btn = (Button) fieldButons.get(i);
            if(field[i/3][i%3] == L_KRESTIK)
                current_btn.setText("X");
            if(field[i/3][i%3] == L_NOLIK)
                current_btn.setText("0");
        }
    }

    // Check for winner
    private boolean hasWon(int mark, int[][] field){
        if(field[0][0] == mark) {
            if ((field[0][1] == mark) && (field[0][2] == mark))
                return true;
            if((field[1][0] == mark) && (field[2][0] == mark))
                return true;
            if((field[1][1] == mark) && (field[2][2] == mark))
                return true;
        }
        if(field[0][2] == mark) {
            if ((field[2][0] == mark) && (field[1][1] == mark))
                return true;
            if((field[2][2] == mark) && (field[1][2] == mark))
                return true;
        }
        if(field[2][1] == mark) {
            if ((field[2][0] == mark) && (field[2][2] == mark))
                return true;
            if((field[0][1] == mark) && (field[1][1] == mark))
                return true;
        }
        if(field[1][1] == mark) {
            if ((field[1][0] == mark) && (field[1][2] == mark))
                return true;
        }
        return false;
    }

    private boolean checkLine(int line[]){
        for (int i = 0; i < 3; i++) {
            if (line[i] == L_FREE)
                return false;
        }
        return true;
    }

    private int eval_sum(int arg[])
    {
        int ret = 0;
        for (int i=0; i<3; i++) ret += arg[i];
        return ret;
    }

    private int find_empty(int arg[])
    {
        for (int i=0; i<3;i++)
            if (arg[i]==0) return i;
        return -1;
    }

    private int clear_arr(int arg[])
    {
        for (int i=0; i<3; i++) arg[i] = L_FREE;
        return 0;
    }

    private boolean check_corn_pos(int l)
    {
        // Флаг занятости центральной клетки
        int c = field[1][1];

        // Проверка для верхнего края
        // Если две клетки края заняты
        if ((field[0][0]==l)&(field[2][0]==l)&(field[1][0]==0)) {
            // Проверить первую угловую
            if (field[0][2]==0) { field[0][2]=l; return true; }
            // Проверить вторую угловую
            if (field[2][2]==0) { field[2][2]=l; return true; }
            // Проверить центральную
            if (c == L_FREE) { field[1][1]=l; return true; }
        }

        // То же для нижнего края
        if ((field[0][2]==l)&(field[2][2]==l)&(field[1][2]==0)) {
            if (field[0][0]==0) { field[0][0]=l; return true; }
            if (field[2][0]==0) { field[2][0]=l; return true; }
        }

        // ... для левго
        if ((field[0][0]==l)&(field[0][2]==l)&(field[0][1]==0)) {
            if (field[2][0]==0) { field[2][0]=l; return true; }
            if (field[2][2]==0) { field[2][2]=l; return true; }
            if (c == L_FREE) { field[1][1]=l; return true; }
        }

        // ... и правого.
        if ((field[2][0]==l)&(field[2][2]==l)&(field[2][1]==0)) {
            if (field[0][0]==0) { field[0][0]=l; return true; }
            if (field[2][0]==0) { field[2][0]=l; return true; }
            if (c == L_FREE) { field[1][1]=l; return true; }
        }

        return false;
    }

    private int checkDanger(int opponentMark, int selfMark){

        int[] opponentField = new int[3];
        int[] selfFiel = new int[3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j] == opponentMark)
                    opponentField[j] = 1;
                else
                    opponentField[j] = L_FREE;
                if (field[i][j] == selfMark)
                    selfFiel[j] = 1;
                else
                    selfFiel[j] = L_FREE;
            }

            if (checkLine(opponentField))
                return opponentMark;
            if(checkLine(selfFiel))
                return selfMark;

            if ((eval_sum(selfFiel)==0)&&(eval_sum(opponentField)==2)) {
                int empt=find_empty(opponentField);
                field[i][empt] = selfMark;
                return 0;
            };

            // Также, только для опасной ситуации
            if ((eval_sum(selfFiel)==2)&&(eval_sum(opponentField)==0)) {

                int empt=find_empty(selfFiel);
                field[i][empt] = selfMark;
                return 0;
            };
            clear_arr(opponentField);
            clear_arr(selfFiel);
        }

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                if (field[i][j] == opponentMark)
                    opponentField[i] = 1;
                else
                    opponentField[i] = L_FREE;
                if (field[i][j] == selfMark)
                    selfFiel[i] = 1;
                else
                    selfFiel[i] = L_FREE;
            }

            if (checkLine(opponentField))
                return opponentMark;
            if(checkLine(selfFiel))
                return selfMark;

            if ((eval_sum(selfFiel)==0)&&(eval_sum(opponentField)==2)) {
                int empt=find_empty(opponentField);
                field[empt][j] = selfMark;
                return 0;
            };

            if ((eval_sum(selfFiel)==2)&&(eval_sum(opponentField)==0)) {

                int empt=find_empty(selfFiel);
                field[empt][j] = selfMark;
                return 0;
            };

            clear_arr(opponentField);
            clear_arr(selfFiel);
        }

        // То же для первой диагонали
        for (int i=0; i<3; i++) {
            if (field[i][i]==selfMark) selfFiel[i]=1;
            if (field[i][i]==opponentMark) opponentField[i]=1;
        }


        if ((eval_sum(selfFiel)==0)&&(eval_sum(opponentField)==2)) {
            int empt=find_empty(opponentField);
            field[empt][empt] = selfMark;
            return 0;
        }

        if ((eval_sum(selfFiel)==2)&&(eval_sum(opponentField)==0)) {

            int empt=find_empty(selfFiel);
            field[empt][empt] = selfMark;
            return 0;
        }

        clear_arr(selfFiel);
        clear_arr(opponentField);

        // То же для второй диагонали
        for (int i=0; i<3; i++) {
            if (field[i][2-i]==selfMark) selfFiel[i]=1;
            if (field[i][2-i]==opponentMark) opponentField[i]=1;

        }


        if ((eval_sum(selfFiel)==0)&&(eval_sum(opponentField)==2)) {
            int empt=find_empty(opponentField);
            field[empt][2-empt] = selfMark;
            return 0;
        }

        if ((eval_sum(selfFiel)==2)&&(eval_sum(opponentField)==0)) {

            int empt=find_empty(selfFiel);
            field[empt][2-empt] = selfMark;
            return 0;
        }
        return 1;


    }


    private void AIMAkeMove(){
        if (!isGameOver){
            if (checkDanger(L_PLAYER, L_AI) == 0) return;

            if (check_game_over()) return;

            // Если все выигрышные позиции заняты...
            if (!check_win_pos())
                for (int x=0; x<3; x++)
                    for (int y=0; y<3; y++)
                        if (field[x][y]==0) {
                            // ...походить на первую попавшуюся свободную клетку
                            field[x][y] = L_AI;
                            return;
                        }
            if (check_corn_pos(L_AI)) return;

            if (field[1][1] == L_FREE){
                field[1][1] = L_AI;
                return;
            }

            boolean isFilled = false;
            while (!isFilled) {
                int id = (int) (Math.random() * 100) % 4;
                if (field[win_pos[id][0]][win_pos[id][1]] == L_FREE) {

                    if(checkDiagonals(L_AI, L_PLAYER) == 1) return;

                    field[win_pos[id][0]][win_pos[id][1]] = L_AI; // Заполнить...
                    isFilled = true;
                }
            }
        }
    }

    private int checkDiagonals(int selfMark, int OpponentMark){
        if(field[1][1] == selfMark && field[2][2] == OpponentMark && field[0][0] == OpponentMark){
            if(field[0][1] == L_FREE){
                field[0][1] = selfMark;
                return 1;
            }else
            if(field[2][1] == L_FREE){
                field[2][1] = selfMark;
                return 1;
            }else
            if(field[1][2] == L_FREE){
                field[1][2] = selfMark;
                return 1;
            }else
            if(field[1][0] == L_FREE){
                field[1][0] = selfMark;
                return 1;
            }
        }
        if(field[1][1] == selfMark && field[0][2] == OpponentMark && field[2][0] == OpponentMark){
            if(field[0][1] == L_FREE){
                field[0][1] = selfMark;
                return 1;
            }else
            if(field[2][1] == L_FREE){
                field[2][1] = selfMark;
                return 1;
            }else
            if(field[1][2] == L_FREE){
                field[1][2] = selfMark;
                return 1;
            }else
            if(field[1][0] == L_FREE){
                field[1][0] = selfMark;
                return 1;
            }
        }
        return 0;
    }

    private void finishTheGame(){
//        visualizeMove();
        isGameOver = true;
        String result = "";
        if(winnerPlayer == L_PLAYER)
            result = "You win!!!";
        if (winnerPlayer == L_AI)
            result = "AI win!!!";
        if (winnerPlayer == 0)
            result = "Draw!!!";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game over");
        alert.setHeaderText(null);
        alert.setContentText(result);
        alert.showAndWait();
    }


    @FXML
    public void initialize(){

        field = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = L_FREE;
            }
        }

        fieldButons.put(0, btn00);
        fieldButons.put(1, btn01);
        fieldButons.put(2, btn02);
        fieldButons.put(3, btn10);
        fieldButons.put(4, btn11);
        fieldButons.put(5, btn12);
        fieldButons.put(6, btn20);
        fieldButons.put(7, btn21);
        fieldButons.put(8, btn22);

        btn00.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int[] btnPos = {0, 0};
                btnClickHandler(btnPos);
            }
        });
        btn01.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int[] btnPos = {0, 1};
                btnClickHandler(btnPos);
            }
        });
        btn02.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int[] btnPos = {0, 2};
                btnClickHandler(btnPos);
            }
        });
        btn10.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int[] btnPos = {1, 0};
                btnClickHandler(btnPos);
            }
        });
        btn11.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int[] btnPos = {1, 1};
                btnClickHandler(btnPos);
            }
        });
        btn12.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int[] btnPos = {1, 2};
                btnClickHandler(btnPos);
            }
        });
        btn20.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int[] btnPos = {2, 0};
                btnClickHandler(btnPos);
            }
        });
        btn21.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int[] btnPos = {2, 1};
                btnClickHandler(btnPos);
            }
        });
        btn22.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int[] btnPos = {2, 2};
                btnClickHandler(btnPos);
            }
        });
    }

    private void btnClickHandler(int pos[]){
        if(isPosFree(pos)){
            field[pos[0]][pos[1]] = L_PLAYER;
            if(hasWon(L_PLAYER, field)){
                winnerPlayer = L_PLAYER;
                finishTheGame();
            }
            else
            {
                if (check_game_over())
                {
                    finishTheGame();
                    return;
                }
                AIMAkeMove();
            }
            visualizeMove();
            if(hasWon(L_AI, field)){
                winnerPlayer = L_AI;
                finishTheGame();
            }
            else
                if (check_game_over())
                    finishTheGame();
        }
    }
}
